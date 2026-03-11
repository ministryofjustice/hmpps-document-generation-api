package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup
import java.util.Comparator.comparing
import java.util.SortedSet

class TemplateGroups(groups: Collection<Group>) {
  val groups: SortedSet<Group> = groups.toSortedSet(comparing(Group::name))

  data class Group(val code: String, val name: String, val description: String, val roles: SortedSet<String>)
}

fun TemplateGroup.asGroup() = TemplateGroups.Group(code, name, description, roles)
