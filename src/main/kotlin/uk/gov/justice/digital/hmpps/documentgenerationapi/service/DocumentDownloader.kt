package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class DocumentDownloader {
  fun docx(bytes: ByteArray, filename: String): ResponseEntity<ByteArray> = ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
    .header(
      HttpHeaders.CONTENT_DISPOSITION,
      ContentDisposition.attachment().filename(filename).build().toString(),
    )
    .header(HttpHeaders.CONTENT_LENGTH, bytes.size.toString())
    .body(bytes)
}
