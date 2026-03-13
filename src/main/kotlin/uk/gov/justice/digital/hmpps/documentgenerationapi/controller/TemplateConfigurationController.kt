package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveTemplateConfiguration
import java.util.UUID

@Hidden
@Controller("/template-configuration")
class TemplateConfigurationController(private val retrieve: RetrieveTemplateConfiguration) {
  @PreAuthorize("hasRole('${Roles.TEMPLATE_CONFIGURATION_RW}')")
  @GetMapping("/{code}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getTemplateConfiguration(@PathVariable code: String): TemplateConfiguration = retrieve.templateConfiguration(code)

  @PreAuthorize("hasRole('${Roles.TEMPLATE_CONFIGURATION_RW}')")
  @GetMapping("/file/{id}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  fun downloadTemplate(@PathVariable id: UUID): ByteArray = retrieve.templateDocument(id)
}
