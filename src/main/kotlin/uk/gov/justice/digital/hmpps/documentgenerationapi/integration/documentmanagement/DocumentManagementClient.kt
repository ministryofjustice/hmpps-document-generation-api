package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import tools.jackson.databind.node.JsonNodeFactory
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateContext
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.retryOnTransientException
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.DocumentType
import java.util.UUID

@Component
class DocumentManagementClient(
  @Qualifier("documentManagementWebClient") private val webClient: WebClient,
) {
  fun uploadTemplate(type: DocumentType, name: String, id: UUID, file: ByteArray, contentType: String?): ManagedDocument = webClient.post()
    .uri("/documents/{type}/{id}", type, id)
    .contentType(MULTIPART_FORM_DATA)
    .header(SERVICE_NAME_KEY, SERVICE_NAME_VALUE)
    .header(USERNAME_KEY, DocumentTemplateContext.retrieve().username)
    .body(BodyInserters.fromMultipartData(file.generateMultipartBody(id, name, contentType)))
    .retrieve()
    .bodyToMono<ManagedDocument>()
    .retryOnTransientException()
    .block()!!

  fun downloadTemplate(id: UUID): ByteArray = webClient.get()
    .uri("/documents/{id}/file", id)
    .header(SERVICE_NAME_KEY, SERVICE_NAME_VALUE)
    .header(USERNAME_KEY, DocumentTemplateContext.retrieve().username)
    .retrieve()
    .bodyToMono<ByteArray>()
    .retryOnTransientException()
    .block()!!

  private fun ByteArray.generateMultipartBody(
    id: UUID,
    name: String,
    contentType: String?,
  ): MultiValueMap<String, HttpEntity<*>> = MultipartBodyBuilder().apply {
    part(
      "file",
      this@generateMultipartBody,
      MediaType.valueOf(contentType?.trim().takeUnless { it.isNullOrEmpty() } ?: "application/octet-stream"),
    ).filename(id.toString())
    part(
      "metadata",
      JsonNodeFactory.instance.objectNode().apply { put("name", name) },
      MediaType.APPLICATION_JSON,
    )
  }.build()

  companion object {
    const val SERVICE_NAME_KEY = "Service-Name"
    const val SERVICE_NAME_VALUE = "hmpps-document-generation-api"
    const val USERNAME_KEY = "Username"
  }
}

data class ManagedDocument(val documentUuid: UUID)
