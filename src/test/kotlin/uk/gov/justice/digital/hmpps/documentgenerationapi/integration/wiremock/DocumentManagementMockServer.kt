package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient.Companion.SERVICE_NAME_KEY
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient.Companion.SERVICE_NAME_VALUE
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.DocumentType
import wiremock.com.google.common.base.Predicates.equalTo
import java.util.UUID

class DocumentManagementMockServer : WireMockServer(9000) {
  fun stubUploadDocument() {
    stubFor(
      post(urlPathTemplate("/documents/{documentType}/{documentId}"))
        .withPathParam("documentType", equalTo(DocumentType.DOCUMENT_GENERATION_TEMPLATES.name))
        .withMultipartRequestBody(aMultipart().withName("file"))
        .withMultipartRequestBody(aMultipart().withName("metadata"))
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

  fun stubDownloadDocument(documentId: UUID, byteArray: ByteArray) {
    stubFor(
      get(urlPathTemplate("/documents/{documentId}/file"))
        .withPathParam("documentId", equalTo(documentId.toString()))
        .withHeader(SERVICE_NAME_KEY, equalTo(SERVICE_NAME_VALUE))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .withBody(byteArray)
            .withStatus(200),
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
