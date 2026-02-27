package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateDetail
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateResponse
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.DocumentTemplateManager
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveDocumentTemplate
import java.util.UUID

@RestController
@RequestMapping(value = ["/templates"])
class TemplateController(
  private val templateManager: DocumentTemplateManager,
  private val retrieveTemplate: RetrieveDocumentTemplate,
) {
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @PutMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun createOrReplaceTemplate(
    @Valid @RequestPart template: TemplateRequest,
    @RequestPart(required = false) file: MultipartFile?,
  ): TemplateResponse = templateManager.createOrReplace(template, file)

  @PreAuthorize("hasAnyRole('${Roles.DOCUMENT_GENERATION_UI}', '${Roles.DOCUMENT_GENERATION_RO}', '${Roles.DOCUMENT_GENERATION_RW}')")
  @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getTemplate(@PathVariable id: UUID): TemplateDetail = retrieveTemplate.byId(id)
}
