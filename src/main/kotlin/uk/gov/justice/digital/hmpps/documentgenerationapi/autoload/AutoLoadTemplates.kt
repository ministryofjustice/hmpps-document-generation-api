package uk.gov.justice.digital.hmpps.documentgenerationapi.autoload

import io.sentry.Sentry
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
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
  private val transactionTemplate: TransactionTemplate,
  private val serviceConfig: ServiceConfig,
  private val configClient: TemplateConfigurationClient,
  private val templateRepository: DocumentTemplateRepository,
  private val variableRepository: TemplateVariableRepository,
  private val groupRepository: TemplateGroupRepository,
  private val dmc: DocumentManagementClient,
) {

  fun refreshAllTemplates() {
    templateRepository.findByCodeNotIn(serviceConfig.includeTemplates).forEach(templateRepository::delete)
    serviceConfig.includeTemplates.mapNotNull { configClient.getTemplateByCode(it) }.forEach { new ->
      try {
        new.mergeTemplate()
      } catch (e: Exception) {
        Sentry.captureException(e)
      }
    }
  }

  private fun TemplateConfiguration.mergeTemplate() {
    val originalReference = templateRepository.findExternalReferenceFromCode(code)
    checkAndReplaceTemplate(originalReference, externalReference, name)
    transactionTemplate.execute {
      (templateRepository.findByCode(code)?.update(code, name, description, instructionText) ?: newTemplate()).apply {
        withExternalReference(this@mergeTemplate.externalReference)
        updateVariables(variables)
        updateGroups(groups)
      }
    }
  }

  private fun checkAndReplaceTemplate(originalReference: UUID?, externalReference: UUID, name: String) {
    if (originalReference != externalReference) {
      val doc = configClient.getTemplate(externalReference)
      dmc.uploadTemplate(DocumentType.DOCUMENT_GENERATION_TEMPLATES, name, externalReference, doc, null)
    }
  }

  private fun TemplateConfiguration.newTemplate(): DocumentTemplate = templateRepository.save(DocumentTemplate(code, name, description, instructionText))

  private fun DocumentTemplate.updateVariables(variables: Collection<TemplateConfiguration.Variable>) {
    val dvs = variableRepository.findByCodeIn(variables.map { it.code }.toSet()).associateBy { it.code }
    withVariables(variables.map { requireNotNull(dvs[it.code]) to it.mandatory }.toSet())
  }

  private fun DocumentTemplate.updateGroups(groups: Collection<TemplateConfiguration.Group>) {
    val grps = groupRepository.findByCodeIn(groups.map { it.code }.toSet()).associateBy { it.code }
    withGroups(groups.map { requireNotNull(grps[it.code]) }.toSet())
  }
}
