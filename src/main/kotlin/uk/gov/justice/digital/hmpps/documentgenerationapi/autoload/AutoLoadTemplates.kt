package uk.gov.justice.digital.hmpps.documentgenerationapi.autoload

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.documentgenerationapi.config.ServiceConfig
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroupRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.templateconfiguration.TemplateConfigurationClient
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.DocumentType
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import java.util.UUID

@Component
@ConditionalOnBean(TemplateConfigurationClient::class)
class AutoLoadTemplates(
  private val serviceConfig: ServiceConfig,
  private val configClient: TemplateConfigurationClient,
  private val templateRepository: DocumentTemplateRepository,
  private val variableRepository: TemplateVariableRepository,
  private val groupRepository: TemplateGroupRepository,
  private val dmc: DocumentManagementClient,
) {
  @EventListener
  fun load(are: ApplicationReadyEvent) {
    refreshAllTemplates()
  }

  fun refreshAllTemplates() {
    serviceConfig.includeTemplates.map { configClient.getTemplateByCode(it) }.forEach { new ->
      try {
        new.mergeTemplate()
      } catch (_: Exception) {
        // TODO: log issues importing templates
      }
    }
  }

  private fun TemplateConfiguration.mergeTemplate() {
    val template = templateRepository.findByCode(code)?.apply {
      update(code, name, description, instructionText)
      checkAndReplaceTemplate(externalReference)
    } ?: newTemplate()
    template.apply {
      updateVariables(variables)
      updateGroups(groups)
    }
  }

  private fun DocumentTemplate.checkAndReplaceTemplate(newExternalReference: UUID) {
    if (externalReference == newExternalReference) return
    val doc = configClient.getTemplate(newExternalReference)
    val res = dmc.uploadTemplate(DocumentType.DOCUMENT_GENERATION_TEMPLATES, name, newExternalReference, doc, null)
    withExternalReference(res.documentUuid)
  }

  private fun TemplateConfiguration.newTemplate(): DocumentTemplate = DocumentTemplate(code, name, description, instructionText, externalReference)

  private fun DocumentTemplate.updateVariables(variables: Collection<TemplateConfiguration.Variable>) {
    val dvs = variableRepository.findByCodeIn(variables.map { it.code }.toSet()).associateBy { it.code }
    withVariables(variables.map { requireNotNull(dvs[it.code]) to it.mandatory }.toSet())
  }

  private fun DocumentTemplate.updateGroups(groups: Collection<TemplateConfiguration.Group>) {
    val grps = groupRepository.findByCodeIn(groups.map { it.code }.toSet()).associateBy { it.code }
    withGroups(groups.map { requireNotNull(grps[it.code]) }.toSet())
  }
}
