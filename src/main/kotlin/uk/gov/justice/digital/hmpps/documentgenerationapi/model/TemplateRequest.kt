package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
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

  data class Variable(val code: String, val required: Boolean)
  data class Group(val code: String)
}

enum class DocumentType {
  DOCUMENT_GENERATION_TEMPLATES,
}
