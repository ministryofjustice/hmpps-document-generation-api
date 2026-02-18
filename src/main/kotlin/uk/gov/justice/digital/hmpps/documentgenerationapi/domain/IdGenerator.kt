package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

object IdGenerator {
  fun newUuid(): UUID = Generators.timeBasedEpochGenerator().generate()
}
