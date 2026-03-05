package uk.gov.justice.digital.hmpps.documentgenerationapi.model

import jakarta.validation.constraints.Pattern

data class GenerateFromTemplate(
  @Pattern(regexp = ".+\\.docx", message = "Only docx files can be generated at this time")
  val filename: String,
  val variables: Map<String, Any>,
)
