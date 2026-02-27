package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroupTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveTemplateGroups

@RestController
@RequestMapping(value = ["/groups"])
class TemplateGroupController(
  private val retrieve: RetrieveTemplateGroups,
) {
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun getTemplateGroups(): TemplateGroups = retrieve.all()

  @PreAuthorize("hasAnyRole('${Roles.DOCUMENT_GENERATION_UI}', '${Roles.DOCUMENT_GENERATION_RO}', '${Roles.DOCUMENT_GENERATION_RW}')")
  @GetMapping("/{groupCode}")
  fun getTemplatesForGroup(@PathVariable groupCode: String): TemplateGroupTemplates = retrieve.templates(groupCode)
}
