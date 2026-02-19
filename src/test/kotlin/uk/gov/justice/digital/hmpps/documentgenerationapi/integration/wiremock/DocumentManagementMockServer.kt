package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient.Companion.SERVICE_NAME_KEY
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient.Companion.SERVICE_NAME_VALUE
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.DocumentType
import java.util.UUID

class DocumentManagementMockServer : WireMockServer(9000) {
  fun stubUploadDocument() {
    stubFor(
      post(urlPathTemplate("/documents/{documentType}/{documentId}"))
        .withPathParam("documentType", equalTo(DocumentType.DOCUMENT_GENERATION_TEMPLATES.name))
        .withMultipartRequestBody(aMultipart().withName("file"))
        .withHeader(SERVICE_NAME_KEY, equalTo(SERVICE_NAME_VALUE))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(generatedDocument())
            .withTransformers("response-template")
            .withStatus(201),
        ),
    )
  }

  fun stubDeleteDocument(id: UUID) {
    stubFor(
      delete(urlMatching("/documents/$id"))
        .withHeader(SERVICE_NAME_KEY, equalTo(SERVICE_NAME_VALUE))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(204),
        ),
    )
  }

  private fun generatedDocument() = """
    {
      "documentUuid": "{{request.path.documentId}}",
      "documentType": "DOCUMENT_GENERATION_TEMPLATES"
    }
  """.trimMargin()
}

class DocumentManagementExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val documentManagementApi = DocumentManagementMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    documentManagementApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    documentManagementApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    documentManagementApi.stop()
  }
}
