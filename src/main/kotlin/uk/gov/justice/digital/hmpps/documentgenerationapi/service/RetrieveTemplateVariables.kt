package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariable
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableDomain
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableDomainRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateVariables

@Service
class RetrieveTemplateVariables(
  private val domainRepository: TemplateVariableDomainRepository,
  private val variableRepository: TemplateVariableRepository,
) {
  fun allByDomain(): TemplateVariables {
    val domains = domainRepository.findAll().sortedBy { it.sequenceNumber }
    val variables = variableRepository.findAll().groupBy { it.domain }
    return TemplateVariables(domains.map { it.withVariables(variables[it.code] ?: emptyList()) })
  }
}

private fun TemplateVariableDomain.withVariables(variables: List<TemplateVariable>) = TemplateVariables.Domain(
  code,
  description,
  variables.sortedBy { it.sequenceNumber }.map { it.asVariable() },
)

private fun TemplateVariable.asVariable() = TemplateVariables.Variable(code, description, type)
