package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import io.swagger.v3.oas.annotations.media.Schema

data class TemplateGroups(val groups: List<Group>) {
  @Schema(name = "TemplateGroups.Group")
  data class Group(val code: String, val name: String, val description: String)
}
