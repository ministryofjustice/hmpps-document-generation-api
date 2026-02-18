package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.util.UUID

@Immutable
@Entity
@Table
class TemplateGroup(
  val code: String,
  val name: String,
  val description: String,
  @Id
  val id: UUID,
)
