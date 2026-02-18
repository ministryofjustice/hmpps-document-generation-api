package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.DocumentTemplateManager

@RestController
@RequestMapping(value = ["/templates"])
class TemplateController(private val templateManager: DocumentTemplateManager) {
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @PutMapping("/{code}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun createOrReplaceTemplate(
    @PathVariable code: String,
    @RequestPart name: String,
    @RequestPart(required = false) description: String?,
    @RequestPart(required = false) file: MultipartFile?,
    @RequestPart(required = false) templateVariables: Set<TemplateRequest.Variable> = emptySet(),
  ) {
    templateManager.createOrReplace(TemplateRequest(code, name, description ?: "", templateVariables), file)
  }
}
