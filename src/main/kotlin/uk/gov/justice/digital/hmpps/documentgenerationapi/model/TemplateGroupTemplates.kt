package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import java.util.UUID

data class TemplateGroupTemplates(val group: NamedDescription, val templates: List<TemplateSummary>)
data class TemplateSummary(val id: UUID, val code: String, val name: String, val description: String)
