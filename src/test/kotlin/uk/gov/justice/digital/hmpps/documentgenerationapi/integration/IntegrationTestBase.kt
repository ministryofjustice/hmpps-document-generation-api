package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.RevisionType
import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditEntity.revisionNumber
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.audit.AuditRevision
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroupRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.container.PostgresContainer
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.DocumentManagementExtension
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.util.UUID

@ExtendWith(HmppsAuthApiExtension::class, DocumentManagementExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var documentTemplateRepository: DocumentTemplateRepository

  @Autowired
  protected lateinit var templateVariableRepository: TemplateVariableRepository

  @Autowired
  protected lateinit var templateGroupRepository: TemplateGroupRepository

  @Autowired
  protected lateinit var transactionTemplate: TransactionTemplate

  @Autowired
  protected lateinit var entityManager: EntityManager

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  protected fun verifyAudit(
    entity: DocumentTemplate,
    revisionType: RevisionType,
    username: String,
  ) {
    transactionTemplate.execute {
      val auditReader = AuditReaderFactory.get(entityManager)
      assertTrue(auditReader.isEntityClassAudited(entity::class.java))

      val revisionNumber =
        auditReader
          .getRevisions(entity::class.java, entity.id)
          .filterIsInstance<Long>()
          .max()

      val entityRevision: Array<*> =
        auditReader
          .createQuery()
          .forRevisionsOfEntity(entity::class.java, false, true)
          .add(revisionNumber().eq(revisionNumber))
          .add(AuditEntity.id().eq(entity.id))
          .resultList
          .first() as Array<*>
      assertThat(entityRevision[2]).isEqualTo(revisionType)

      val auditRevision = entityRevision[1] as AuditRevision
      assertThat(auditRevision.username).isEqualTo(username)
    }
  }

  protected final inline fun <reified T : Any> WebTestClient.ResponseSpec.successResponse(status: HttpStatus = HttpStatus.OK): T = expectStatus().isEqualTo(status)
    .expectBody<T>()
    .returnResult().responseBody!!

  protected final fun WebTestClient.ResponseSpec.errorResponse(status: HttpStatus): ErrorResponse = expectStatus().isEqualTo(status)
    .expectBody<ErrorResponse>()
    .returnResult().responseBody!!

  protected fun documentTemplate(
    code: String = word(16),
    name: String = "Name of $code",
    description: String = "Description of $code => $name",
    externalReference: UUID = newUuid(),
    id: UUID = newUuid(),
  ): DocumentTemplate = DocumentTemplate(code, name, description, setOf(), externalReference, id)

  protected fun givenDocumentTemplate(
    docTemplate: DocumentTemplate = documentTemplate(),
    requiredVariables: Map<String, Boolean> = mapOf(),
    requiredGroups: Set<String> = setOf(),
  ): DocumentTemplate = transactionTemplate.execute {
    val variables = templateVariableRepository.findByCodeIn(requiredVariables.keys).associateBy { it.code }
    val groups = templateGroupRepository.findByCodeIn(requiredGroups).associateBy { it.code }
    documentTemplateRepository.save(
      docTemplate
        .withVariables(
          requiredVariables.map { requireNotNull(variables[it.key]) { "Template variable not recognised" } to it.value }
            .toSet(),
        )
        .withGroups(
          requiredGroups.map { requireNotNull(groups[it]) { "Template group not recognised" } }.toSet(),
        ),
    )
  }

  protected fun findDocumentTemplate(code: String): DocumentTemplate? = transactionTemplate.execute {
    documentTemplateRepository.findByCode(code)?.also { dt ->
      // pull variables/groups inside of transaction to avoid lazy loading exceptions in tests
      dt.variables().forEach { it.variable.code }
      dt.groups().forEach { it.code }
    }
  }

  companion object {
    private val pgContainer = PostgresContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.also {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.datasource.username", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
      }

      System.setProperty("aws.region", "eu-west-2")
    }
  }
}
