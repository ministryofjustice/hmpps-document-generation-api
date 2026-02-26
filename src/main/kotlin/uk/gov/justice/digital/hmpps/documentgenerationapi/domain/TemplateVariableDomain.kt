package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table
class TemplateVariableDomain(
  @Id
  val code: String,
  val description: String,
  val sequenceNumber: Int,
)

interface TemplateVariableDomainRepository : JpaRepository<TemplateVariableDomain, String>
