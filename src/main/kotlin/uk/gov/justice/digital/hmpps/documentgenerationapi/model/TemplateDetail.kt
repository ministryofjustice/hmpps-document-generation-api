package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import java.util.UUID

data class TemplateDetail(
  val id: UUID,
  val code: String,
  val name: String,
  val description: String,
  val groups: List<TemplateGroups.Group>,
  val variables: TemplateVariables,
)
