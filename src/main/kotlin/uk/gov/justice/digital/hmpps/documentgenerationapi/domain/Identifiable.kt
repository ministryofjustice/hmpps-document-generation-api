package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import java.util.UUID

interface Identifiable {
  val id: UUID
}
