package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.envers.RevisionType
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.config.CaseloadIdHeader
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.DocumentManagementExtension.Companion.documentManagementApi
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.ManageUsersExtension.Companion.manageUsers
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.ManageUsersServer.Companion.user
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.GenerateFromTemplate
import java.io.File
import java.util.UUID

class GenerateDocumentIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .post()
      .uri(GENERATE_DOCUMENT_URL, newUuid())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    generateDocument(
      newUuid(),
      generationRequest(),
      null,
      username(),
      "ANY_OTHER",
    ).expectStatus().isForbidden
  }

  @Test
  fun `200 can generate document from template`() {
    val template = givenDocumentTemplate()
    val docTemplate = File("src/test/resources/test-template.dotx")
    documentManagementApi.stubDownloadDocument(template.externalReference, docTemplate.readBytes())

    val request = generationRequest()
    val username = username()
    val caseloadId = word(3)
    generateDocument(
      template.id,
      request,
      File("src/test/resources/image.jpg").readBytes(),
      username,
      caseloadId = caseloadId,
    ).expectStatus().isOk

    val docGenRequest = docGenReqRepository.findAll().first { it.template.id == template.id }
    assertThat(docGenRequest.request).isEqualTo(request)

    verifyAudit(docGenRequest, RevisionType.ADD, username, caseloadId)
  }

  @Test
  fun `200 can generate document without an image`() {
    val template = givenDocumentTemplate()
    val docTemplate = File("src/test/resources/test-template.dotx")
    documentManagementApi.stubDownloadDocument(template.externalReference, docTemplate.readBytes())

    val request = generationRequest()
    val user = user(username(), caseloadId = word(3))
    manageUsers.stubFindUser(user.username, user)
    generateDocument(
      template.id,
      request,
      null,
      user.username,
    ).expectStatus().isOk

    val docGenRequest = docGenReqRepository.findAll().first { it.template.id == template.id }
    assertThat(docGenRequest.request).isEqualTo(request)

    verifyAudit(docGenRequest, RevisionType.ADD, user.username, user.caseloadId)
  }

  private fun generationRequest(
    filename: String = word(10) + ".docx",
    variables: Map<String, Any> = mapOf("perName" to "Sirius Black", "prsnName" to "Azkaban"),
  ) = GenerateFromTemplate(filename, variables)

  private fun generateDocument(
    id: UUID = newUuid(),
    request: GenerateFromTemplate = generationRequest(),
    image: ByteArray? = null,
    username: String = username(),
    role: String? = listOf(
      Roles.DOCUMENT_GENERATION_UI,
      Roles.DOCUMENT_GENERATION_RO,
      Roles.DOCUMENT_GENERATION_RW,
    ).random(),
    caseloadId: String? = null,
  ) = webTestClient
    .post()
    .uri(GENERATE_DOCUMENT_URL, id)
    .contentType(MediaType.MULTIPART_FORM_DATA)
    .body(BodyInserters.fromMultipartData(generateMultipartBody(request, image)))
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .headers { headers ->
      caseloadId?.also { headers.put(CaseloadIdHeader.NAME, listOf(it)) }
    }
    .exchange()

  private fun generateMultipartBody(
    request: GenerateFromTemplate,
    image: ByteArray?,
  ): MultiValueMap<String, HttpEntity<*>> = MultipartBodyBuilder().apply {
    part("data", request)
    image?.also { part("perImage", it, MediaType.IMAGE_JPEG) }
  }.build()

  companion object {
    const val GENERATE_DOCUMENT_URL = "/templates/{id}/document"
  }
}
