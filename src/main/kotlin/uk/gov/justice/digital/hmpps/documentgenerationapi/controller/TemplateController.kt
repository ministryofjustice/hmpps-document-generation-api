package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateResponse
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.DocumentTemplateManager

@RestController
@RequestMapping(value = ["/templates"])
class TemplateController(private val templateManager: DocumentTemplateManager) {
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @PutMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun createOrReplaceTemplate(
    @RequestPart template: TemplateRequest,
    @RequestPart(required = false) file: MultipartFile?,
  ): TemplateResponse = templateManager.createOrReplace(template, file)
}
