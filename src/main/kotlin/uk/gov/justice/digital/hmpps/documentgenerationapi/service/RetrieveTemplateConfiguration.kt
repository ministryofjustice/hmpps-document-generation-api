package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.getByCode
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import java.util.UUID

@Service
class RetrieveTemplateConfiguration(
  private val templateRepository: DocumentTemplateRepository,
  private val dmc: DocumentManagementClient,
) {
  @Transactional(readOnly = true)
  fun templateConfiguration(code: String): TemplateConfiguration = templateRepository.getByCode(code).config()
  fun templateDocument(id: UUID) = dmc.downloadTemplate(id)
}

private fun DocumentTemplate.config() = TemplateConfiguration(
  id = id,
  code = code,
  name = name,
  description = description,
  instructionText = instructionText,
  externalReference = externalReference,
  variables = variables().map { TemplateConfiguration.Variable(it.variable.code, it.mandatory) },
  groups = groups().map { TemplateConfiguration.Group(it.code) },
)
