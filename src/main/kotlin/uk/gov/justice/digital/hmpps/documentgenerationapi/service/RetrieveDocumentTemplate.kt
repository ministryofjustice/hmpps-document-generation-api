package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateVariable
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableDomain
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariableDomainRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.getDocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateDetail
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateVariables
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.asGroup
import java.util.UUID

@Service
@Transactional
class RetrieveDocumentTemplate(
  private val templateRepository: DocumentTemplateRepository,
  private val domainRepository: TemplateVariableDomainRepository,
) {
  fun byId(id: UUID): TemplateDetail {
    val template = templateRepository.getDocumentTemplate(id)
    val domains = domainRepository.findAll().associateBy { it.code }
    return template.detail { domainCode -> requireNotNull(domains[domainCode]) }
  }
}

private fun DocumentTemplate.detail(domainProvider: (String) -> TemplateVariableDomain) = TemplateDetail(
  id,
  code,
  name,
  description,
  groups().detail(),
  variables().detail(domainProvider),
)

private fun Set<TemplateGroup>.detail() = map(TemplateGroup::asGroup).sortedBy { it.name }
private fun Set<DocumentTemplateVariable>.detail(domainProvider: (String) -> TemplateVariableDomain) = groupBy { it.variable.domain }.map { e ->
  val domain = domainProvider(e.key)
  TemplateVariables.Domain(
    domain.code,
    domain.description,
    e.value
      .sortedBy { it.variable.sequenceNumber }
      .map { TemplateVariables.Variable(it.variable.code, it.variable.description, it.variable.type) },
  )
}.let(::TemplateVariables)
