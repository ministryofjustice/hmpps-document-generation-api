package uk.gov.justice.digital.hmpps.documentgenerationapi.model

data class TemplateRequest(
  val code: String,
  val name: String,
  val description: String,
  val variables: Set<Variable>,
) {
  val type = DocumentType.DOCUMENT_GENERATION_TEMPLATES

  data class Variable(val domain: String, val code: String, val required: Boolean)
}

enum class DocumentType {
  DOCUMENT_GENERATION_TEMPLATES,
}
