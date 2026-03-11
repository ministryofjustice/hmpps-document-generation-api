package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroupRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.getGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroupTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateSummary
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.asGroup

@Service
class RetrieveTemplateGroups(
  private val groupRepository: TemplateGroupRepository,
  private val templateRepository: DocumentTemplateRepository,
) {
  fun all(): TemplateGroups = groupRepository.findAll().map(TemplateGroup::asGroup).let { TemplateGroups(it) }

  fun templates(groupCode: String): TemplateGroupTemplates {
    val group = groupRepository.getGroup(groupCode)
    val templates = templateRepository.findByGroupCode(group.id)
    return TemplateGroupTemplates(group.asGroup(), templates.map { it.asTemplate() })
  }
}
private fun DocumentTemplate.asTemplate() = TemplateSummary(id, code, name, description, instructionText)
