package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateVariables
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveTemplateVariables

@RestController
@RequestMapping(value = ["/variables"])
@PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
class TemplateVariableController(
  private val retrieve: RetrieveTemplateVariables,
) {
  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun getTemplateVariables(): TemplateVariables = retrieve.allByDomain()
}
