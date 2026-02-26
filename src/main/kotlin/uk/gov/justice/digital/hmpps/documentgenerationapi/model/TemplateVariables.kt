package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariable

data class TemplateVariables(val domains: List<Domain>) {
  @Schema(name = "TemplateVariables.Domain")
  data class Domain(val code: String, val description: String, val variables: List<Variable>)

  @Schema(name = "TemplateVariables.Variable")
  data class Variable(val code: String, val description: String, val type: TemplateVariable.Type)
}
