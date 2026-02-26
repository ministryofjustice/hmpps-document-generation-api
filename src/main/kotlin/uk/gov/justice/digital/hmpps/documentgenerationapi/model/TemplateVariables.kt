package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariable

data class TemplateVariables(val domains: List<Domain>) {
  data class Domain(val code: String, val description: String, val variables: List<Variable>)
  data class Variable(val code: String, val description: String, val type: TemplateVariable.Type)
}
