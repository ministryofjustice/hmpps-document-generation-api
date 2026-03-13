package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.RevisionType
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.documentgenerationapi.autoload.AutoLoadTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateContext.Companion.SYSTEM_USERNAME
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.DocumentManagementExtension.Companion.documentManagementApi
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.TemplateConfigurationExtension
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.TemplateConfigurationExtension.Companion.templateConfigurationApi
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.TemplateConfigurationServer.Companion.templateConfig
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import java.io.File

const val TEST_TEMPLATE_CODE = "TEST_TEMPLATE_1"
const val TEST_TEMPLATE_FILENAME = "src/test/resources/test-template.dotx"

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(TemplateConfigurationExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["service.include-templates=$TEST_TEMPLATE_CODE", "service.allow-pulling-templates=true"],
)
class AutoLoadTemplateIntTest(@Autowired private val autoload: AutoLoadTemplates) : IntegrationTestBase() {

  @Order(1)
  @Test
  fun `can load new template and remove unused template`() {
    val toRemove = givenDocumentTemplate(documentTemplate())
    val templateConfig = templateConfig(
      code = TEST_TEMPLATE_CODE,
      groups = listOf(
        TemplateConfiguration.Group("TEMPORARY_ABSENCE"),
      ),
      variables = listOf(
        TemplateConfiguration.Variable("perPrsnNo", true),
        TemplateConfiguration.Variable("perName", false),
        TemplateConfiguration.Variable("perDob", false),
      ),
    )
    val document = File(TEST_TEMPLATE_FILENAME).readBytes()
    templateConfigurationApi.stubGetTemplateConfig(templateConfig)
    templateConfigurationApi.stubDownloadDocument(templateConfig.externalReference, document)
    documentManagementApi.stubUploadDocument()

    autoload.refreshAllTemplates()

    val removed = findDocumentTemplate(toRemove.id)
    assertThat(removed).isNull()

    val newId = requireNotNull(documentTemplateRepository.findByCode(templateConfig.code)).id
    val saved = requireNotNull(findDocumentTemplate(newId))
    assertThat(saved.name).isEqualTo(templateConfig.name)
    assertThat(saved.description).isEqualTo(templateConfig.description)
    assertThat(saved.instructionText).isEqualTo(templateConfig.instructionText)
    assertThat(saved.externalReference).isEqualTo(templateConfig.externalReference)
    assertThat(saved.groups().map { it.code }.single()).isEqualTo("TEMPORARY_ABSENCE")
    assertThat(saved.variables().map { it.variable.code }).containsExactlyInAnyOrder("perPrsnNo", "perName", "perDob")

    verifyAudit(saved, RevisionType.ADD, SYSTEM_USERNAME)
  }

  @Order(2)
  @Test
  fun `can update template and replace template document`() {
    val templateConfig = templateConfig(
      code = TEST_TEMPLATE_CODE,
      description = "A change in the description",
      instructionText = "The instructions are different now",
      groups = listOf(
        TemplateConfiguration.Group("EXTERNAL_MOVEMENT"),
      ),
      variables = listOf(
        TemplateConfiguration.Variable("perPrsnNo", true),
        TemplateConfiguration.Variable("perName", false),
        TemplateConfiguration.Variable("prsnName", true),
      ),
    )
    val document = File(TEST_TEMPLATE_FILENAME).readBytes()
    templateConfigurationApi.stubGetTemplateConfig(templateConfig)
    templateConfigurationApi.stubDownloadDocument(templateConfig.externalReference, document)
    documentManagementApi.stubUploadDocument()

    autoload.refreshAllTemplates()

    val id = requireNotNull(documentTemplateRepository.findByCode(templateConfig.code)).id
    val saved = requireNotNull(findDocumentTemplate(id))
    assertThat(saved.name).isEqualTo(templateConfig.name)
    assertThat(saved.description).isEqualTo(templateConfig.description)
    assertThat(saved.instructionText).isEqualTo(templateConfig.instructionText)
    assertThat(saved.externalReference).isEqualTo(templateConfig.externalReference)
    assertThat(saved.groups().map { it.code }.single()).isEqualTo("EXTERNAL_MOVEMENT")
    assertThat(saved.variables().map { it.variable.code to it.mandatory })
      .containsExactlyInAnyOrder("perPrsnNo" to true, "perName" to false, "prsnName" to true)

    verifyAudit(saved, RevisionType.MOD, SYSTEM_USERNAME)
  }

  @Order(3)
  @Test
  fun `can update template without changing the template document`() {
    val existing = requireNotNull(documentTemplateRepository.findByCode(TEST_TEMPLATE_CODE))
    val templateConfig = templateConfig(
      code = TEST_TEMPLATE_CODE,
      name = "Changed Named",
      instructionText = null,
      externalReference = existing.externalReference,
      groups = listOf(
        TemplateConfiguration.Group("TEMPORARY_ABSENCE"),
      ),
      variables = listOf(
        TemplateConfiguration.Variable("perPrsnNo", true),
        TemplateConfiguration.Variable("perPnc", true),
        TemplateConfiguration.Variable("prsnName", false),
      ),
    )
    val document = File(TEST_TEMPLATE_FILENAME).readBytes()
    templateConfigurationApi.stubGetTemplateConfig(templateConfig)
    templateConfigurationApi.stubDownloadDocument(templateConfig.externalReference, document)
    documentManagementApi.stubUploadDocument()

    autoload.refreshAllTemplates()

    val id = requireNotNull(documentTemplateRepository.findByCode(templateConfig.code)).id
    val saved = requireNotNull(findDocumentTemplate(id))
    assertThat(saved.name).isEqualTo(templateConfig.name)
    assertThat(saved.description).isEqualTo(templateConfig.description)
    assertThat(saved.instructionText).isEqualTo(templateConfig.instructionText)
    assertThat(saved.externalReference).isEqualTo(templateConfig.externalReference)
    assertThat(saved.groups().map { it.code }.single()).isEqualTo("TEMPORARY_ABSENCE")
    assertThat(saved.variables().map { it.variable.code to it.mandatory })
      .containsExactlyInAnyOrder("perPrsnNo" to true, "perPnc" to true, "prsnName" to false)

    verifyAudit(saved, RevisionType.MOD, SYSTEM_USERNAME)
  }
}
