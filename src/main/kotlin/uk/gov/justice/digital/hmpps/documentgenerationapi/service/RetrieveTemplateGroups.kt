package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroupRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups

@Service
class RetrieveTemplateGroups(
  private val groupRepository: TemplateGroupRepository,
) {
  fun all(): TemplateGroups = groupRepository.findAll().sortedBy { it.name }.map(TemplateGroup::asGroup).let { TemplateGroups(it) }
}

private fun TemplateGroup.asGroup() = TemplateGroups.Group(code, name, description)
