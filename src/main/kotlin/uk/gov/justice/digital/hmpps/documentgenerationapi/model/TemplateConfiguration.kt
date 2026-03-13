package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import java.util.UUID

data class TemplateConfiguration(
  val id: UUID,
  val code: String,
  val name: String,
  val description: String,
  val instructionText: String?,
  val externalReference: UUID,
  val variables: List<Variable>,
  val groups: List<Group>,
) {
  data class Variable(val code: String, val mandatory: Boolean)
  data class Group(val code: String)
}
