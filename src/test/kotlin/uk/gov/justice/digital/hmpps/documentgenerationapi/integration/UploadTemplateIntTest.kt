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
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateVariable
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.VariableKey
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock.DocumentManagementExtension.Companion.documentManagementApi
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import java.io.File

class UploadTemplateIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(UPLOAD_TEMPLATE_URL, "NO_TOKEN")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    uploadTemplate(
      templateRequest("WRONG_ROLE"),
      null,
      username(),
      "ANY_OTHER",
    ).expectStatus().isForbidden
  }

  @Test
  fun `200 ok - can update template name and description without overriding file`() {
    val username = username()
    val dt = givenDocumentTemplate()
    val request = templateRequest(dt.code, "Updated name", "Updated description")
    uploadTemplate(request, null, username).expectStatus().isNoContent

    val saved = requireNotNull(documentTemplateRepository.findByCode(request.code))
    assertThat(saved.name).isEqualTo(request.name)
    assertThat(saved.description).isEqualTo(request.description)
    assertThat(saved.externalReference).isEqualTo(dt.externalReference)

    verifyAudit(saved, RevisionType.MOD, username)
  }

  @Test
  fun `200 ok when uploading a new document`() {
    val username = username()
    val request = templateRequest(
      word(8),
      variables = setOf(
        TemplateRequest.Variable("PERSON", "PERSON__PRISON_NUMBER", true),
        TemplateRequest.Variable("PERSON", "PERSON__NAME", true),
        TemplateRequest.Variable("PERSON", "PERSON__DATE_OF_BIRTH", false),
      ),
    )
    documentManagementApi.stubUploadDocument()

    uploadTemplate(request, File(word(6)), username).expectStatus().isNoContent

    val saved = requireNotNull(findDocumentTemplate(request.code))
    assertThat(saved.name).isEqualTo(request.name)
    assertThat(saved.description).isEqualTo(request.description)
    assertThat(saved.externalReference).isNotNull
    assertThat(saved.variables()).hasSize(3)
    saved.variables().forEach { it.verifyAgainst(request) }

    verifyAudit(saved, RevisionType.ADD, username)
  }

  @Test
  fun `200 ok when replacing an existing document`() {
    val username = username()
    val dt = givenDocumentTemplate(
      variableKeys = setOf(
        VariableKey("PERSON", "PERSON__PRISON_NUMBER") to true,
        VariableKey("PERSON", "PERSON__DATE_OF_BIRTH") to false,
      ),
    )
    val request = templateRequest(
      dt.code,
      variables = setOf(
        TemplateRequest.Variable("PERSON", "PERSON__PRISON_NUMBER", true),
        TemplateRequest.Variable("PERSON", "PERSON__NAME", true),
      ),
    )
    documentManagementApi.stubUploadDocument()
    documentManagementApi.stubDeleteDocument(dt.externalReference)

    uploadTemplate(request, File(word(6)), username).expectStatus().isNoContent

    val saved = requireNotNull(findDocumentTemplate(dt.code))
    assertThat(saved.name).isEqualTo(request.name)
    assertThat(saved.description).isEqualTo(request.description)
    assertThat(saved.externalReference).isNotEqualTo(dt.externalReference)
    assertThat(saved.variables()).hasSize(2)
    saved.variables().forEach { it.verifyAgainst(request) }

    verifyAudit(saved, RevisionType.MOD, username)
  }

  private fun templateRequest(
    code: String,
    name: String = "Name of $code",
    description: String = "Description of $code : $name",
    variables: Set<TemplateRequest.Variable> = setOf(),
  ) = TemplateRequest(code, name, description, variables)

  private fun uploadTemplate(
    request: TemplateRequest,
    file: File? = null,
    username: String = username(),
    role: String? = Roles.DOCUMENT_GENERATION_UI,
  ) = webTestClient
    .put()
    .uri(UPLOAD_TEMPLATE_URL, request.code)
    .contentType(MediaType.MULTIPART_FORM_DATA)
    .body(BodyInserters.fromMultipartData(generateMultipartBody(request, file)))
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  private fun generateMultipartBody(request: TemplateRequest, file: File?): MultiValueMap<String, HttpEntity<*>> = MultipartBodyBuilder().apply {
    part("name", request.name)
    part("description", request.description)
    part("templateVariables", request.variables)
    file?.also { part("file", it).filename(it.name) }
  }.build()

  companion object {
    const val UPLOAD_TEMPLATE_URL = "/templates/{code}"

    fun DocumentTemplateVariable.verifyAgainst(request: TemplateRequest) {
      val requested = request.variables.first { it.domain == this.variable.domain && it.code == this.variable.code }
      assertThat(mandatory).isEqualTo(requested.required)
    }
  }
}
