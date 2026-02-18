package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table
class TemplateVariableDomain(
  @Id
  val code: String,
  val description: String,
  val sequenceNumber: Int,
)
