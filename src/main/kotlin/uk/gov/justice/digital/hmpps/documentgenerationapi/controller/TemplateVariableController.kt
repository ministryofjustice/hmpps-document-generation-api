package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateVariables
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveTemplateVariables
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = "Template variables")
@RestController
@RequestMapping(value = ["/variables"])
@PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
class TemplateVariableController(
  private val retrieve: RetrieveTemplateVariables,
) {
  @Operation(
    summary = "Retrieve all template variables.",
    description = "Returns all supported template variables grouped by domain.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Template variables found."),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun getTemplateVariables(): TemplateVariables = retrieve.allByDomain()
}
