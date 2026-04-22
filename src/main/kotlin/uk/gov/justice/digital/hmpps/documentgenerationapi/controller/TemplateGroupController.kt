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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.config.TEMPLATE_GROUPS
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroupTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveTemplateGroups
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Tag(name = TEMPLATE_GROUPS)
@RestController
@RequestMapping(value = ["/groups"])
class TemplateGroupController(
  private val retrieve: RetrieveTemplateGroups,
) {
  @Operation(
    summary = "Retrieve all template groups.",
    description = "Returns all defined template groups. Callers should use only the group relevant to their service.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Template groups found."),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @GetMapping(produces = [APPLICATION_JSON_VALUE])
  fun getTemplateGroups(): TemplateGroups = retrieve.all()

  @Operation(
    summary = "Retrieve templates for a group.",
    description = "Returns the templates assigned to the specified group.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Templates found."),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "Group not found.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @PreAuthorize("hasAnyRole('${Roles.DOCUMENT_GENERATION_UI}', '${Roles.DOCUMENT_GENERATION_RO}', '${Roles.DOCUMENT_GENERATION_RW}')")
  @GetMapping("/{groupCode}")
  fun getTemplatesForGroup(@PathVariable groupCode: String): TemplateGroupTemplates = retrieve.templates(groupCode)
}
