package uk.gov.justice.digital.hmpps.documentgenerationapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.config.CaseloadIdHeader
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.GenerateFromTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateDetail
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateResponse
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.DocumentGenerator
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.DocumentTemplateManager
import uk.gov.justice.digital.hmpps.documentgenerationapi.service.RetrieveDocumentTemplate
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Tag(name = "Templates")
@RestController
@RequestMapping(value = ["/templates"])
class TemplateController(
  private val templateManager: DocumentTemplateManager,
  private val retrieveTemplate: RetrieveDocumentTemplate,
  private val generate: DocumentGenerator,
) {
  @Operation(
    summary = "Create or replace a template.",
    description = "Creates a new template or replaces an existing one with the same code. " +
      "Accepts a multipart request containing the template metadata and optionally a .dotx file. " +
      "If no file is supplied, the existing template file is retained.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Template created or replaced."),
      ApiResponse(responseCode = "400", description = "Bad request.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @CaseloadIdHeader
  @PreAuthorize("hasRole('${Roles.DOCUMENT_GENERATION_UI}')")
  @PutMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
  fun createOrReplaceTemplate(
    @Valid @RequestPart template: TemplateRequest,
    @RequestPart(required = false) file: MultipartFile?,
  ): TemplateResponse = templateManager.createOrReplace(template, file)

  @Operation(
    summary = "Retrieve a template by its unique identifier.",
    description = "Returns the template detail including its variables, group assignments, and instruction text.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Template found."),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "Template not found.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @PreAuthorize("hasAnyRole('${Roles.DOCUMENT_GENERATION_UI}', '${Roles.DOCUMENT_GENERATION_RO}', '${Roles.DOCUMENT_GENERATION_RW}')")
  @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getTemplate(@PathVariable id: UUID): TemplateDetail = retrieveTemplate.byId(id)

  @Operation(
    summary = "Generate a document from a template.",
    description = "Applies the supplied variable values to the template and returns the generated Word document as an octet stream. " +
      "Accepts a multipart request. " +
      "If the template uses the `perImage` variable, the prisoner image must be fetched by the caller from the Prison API and supplied as a named multipart file part (`perImage`) — it cannot be passed as a variable value. " +
      "The generation event is audited.",
  )
  @ApiResponses(
    value = [
      ApiResponse(responseCode = "200", description = "Document generated."),
      ApiResponse(responseCode = "400", description = "Bad request.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "401", description = "Unauthorised, requires a valid Oauth2 token.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden, requires an appropriate role.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "Template not found.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @CaseloadIdHeader
  @PreAuthorize("hasAnyRole('${Roles.DOCUMENT_GENERATION_UI}', '${Roles.DOCUMENT_GENERATION_RO}', '${Roles.DOCUMENT_GENERATION_RW}')")
  @PostMapping("/{id}/document", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  fun generateDocumentFromTemplate(
    @PathVariable id: UUID,
    @Valid @RequestPart data: GenerateFromTemplate,
    @RequestPart(required = false, name = "perImage") personImage: MultipartFile?,
  ): ResponseEntity<ByteArray> = generate.fromTemplate(id, data, personImage)
}
