package uk.gov.justice.digital.hmpps.documentgenerationapi.model

data class TemplateRequest(
  val code: String,
  val name: String,
  val description: String,
  val variables: Set<Variable>,
  val groups: Set<Group>,
) {
  val type = DocumentType.DOCUMENT_GENERATION_TEMPLATES

  data class Variable(val code: String, val required: Boolean)
  data class Group(val code: String)
}

enum class DocumentType {
  DOCUMENT_GENERATION_TEMPLATES,
}
