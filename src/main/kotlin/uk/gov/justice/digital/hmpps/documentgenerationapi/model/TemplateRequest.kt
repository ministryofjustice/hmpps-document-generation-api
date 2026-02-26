package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class TemplateRequest(
  val id: UUID?,
  val code: String,
  val name: String,
  val description: String?,
  val variables: Set<Variable> = emptySet(),
  val groups: Set<Group> = emptySet(),
) {
  @JsonIgnore
  val type = DocumentType.DOCUMENT_GENERATION_TEMPLATES

  @Schema(name = "TemplateRequest.Variable")
  data class Variable(val code: String, val required: Boolean)

  @Schema(name = "TemplateRequest.Group")
  data class Group(val code: String)
}

enum class DocumentType {
  DOCUMENT_GENERATION_TEMPLATES,
}
