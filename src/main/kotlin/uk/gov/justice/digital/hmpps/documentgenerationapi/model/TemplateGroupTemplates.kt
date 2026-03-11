package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import java.util.Comparator.comparing
import java.util.SortedSet
import java.util.UUID

class TemplateGroupTemplates(val group: TemplateGroups.Group, templates: Collection<TemplateSummary>) {
  val templates: SortedSet<TemplateSummary> = templates.toSortedSet(comparing(TemplateSummary::name))
}

data class TemplateSummary(val id: UUID, val code: String, val name: String, val description: String)
