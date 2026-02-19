package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateContext
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import java.util.UUID

@Component
class DocumentManagementClient(@Qualifier("documentManagementWebClient") private val webClient: WebClient) {
  fun uploadDocument(request: TemplateRequest, id: UUID, file: MultipartFile): ManagedDocument = webClient.post()
    .uri("/documents/{type}/{id}", request.type, id)
    .contentType(MULTIPART_FORM_DATA)
    .header(SERVICE_NAME_KEY, SERVICE_NAME_VALUE)
    .header(USERNAME_KEY, DocumentTemplateContext.retrieve().username)
    .body(BodyInserters.fromMultipartData(file.generateMultipartBody(id)))
    .retrieve()
    .bodyToMono<ManagedDocument>()
    .retryOnTransientException()
    .block()!!

  private fun MultipartFile.generateMultipartBody(id: UUID): MultiValueMap<String, HttpEntity<*>> = MultipartBodyBuilder().apply {
    part(
      "file",
      this@generateMultipartBody.resource,
      MediaType.valueOf(this@generateMultipartBody.contentType?.trim().takeUnless { it.isNullOrEmpty() } ?: "application/octet-stream"),
    ).filename(id.toString())
  }.build()

  companion object {
    const val SERVICE_NAME_KEY = "Service-Name"
    const val SERVICE_NAME_VALUE = "hmpps-document-generation-api"
    const val USERNAME_KEY = "Username"
  }
}

data class ManagedDocument(val documentUuid: UUID)
