package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroupRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.getGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.NamedDescription
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroupTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups

@Service
class RetrieveTemplateGroups(
  private val groupRepository: TemplateGroupRepository,
  private val templateRepository: DocumentTemplateRepository,
) {
  fun all(): TemplateGroups = groupRepository.findAll().sortedBy { it.name }.map(TemplateGroup::asGroup).let { TemplateGroups(it) }

  fun templates(groupCode: String): TemplateGroupTemplates {
    val group = groupRepository.getGroup(groupCode)
    val templates = templateRepository.findByGroupCode(group.id)
    return TemplateGroupTemplates(group.asGroup(), templates.map { it.asTemplate() }.sortedBy { it.name })
  }
}

private fun TemplateGroup.asGroup() = NamedDescription(code, name, description)
private fun DocumentTemplate.asTemplate() = NamedDescription(code, name, description)
