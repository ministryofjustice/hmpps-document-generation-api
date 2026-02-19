package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateRequest

@Service
class DocumentTemplateManager(
  private val transactionTemplate: TransactionTemplate,
  private val dmc: DocumentManagementClient,
  private val docTemplateRepository: DocumentTemplateRepository,
  private val variableRepository: TemplateVariableRepository,
) {
  fun createOrReplace(request: TemplateRequest, file: MultipartFile?) {
    val existing = docTemplateRepository.findByCode(request.code)
    file ?: check(existing != null) { "Attempt to create a template without a file" }
    val dt = existing?.update(request.name, request.description) ?: request.asDocumentTemplate()
    file?.also {
      val er = existing?.withNewExternalReference()?.externalReference ?: dt.externalReference
      val managed = dmc.uploadDocument(request, er, it)
      check(managed.documentUuid == er)
    }
    transactionTemplate.executeWithoutResult {
      val saved = docTemplateRepository.save(dt)
      val varsRequired = request.requiredVariables()
      val templateVariables = variableRepository.findByCodeIn(varsRequired.keys).associateBy { tv -> tv.code }
      val validateTemplateVariable = { code: String ->
        requireNotNull(templateVariables[code]) { "Template variable not recognised" }
      }
      saved.withVariables(request.variables.map { validateTemplateVariable(it.code) to it.required }.toSet())
    }
  }
}

private fun TemplateRequest.asDocumentTemplate(): DocumentTemplate = DocumentTemplate(code, name, description, setOf())
private fun TemplateRequest.requiredVariables() = variables.associate { it.code to it.required }
