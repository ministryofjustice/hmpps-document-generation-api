package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateGroup

data class NamedDescription(val code: String, val name: String, val description: String)

fun TemplateGroup.asGroup() = NamedDescription(code, name, description)
