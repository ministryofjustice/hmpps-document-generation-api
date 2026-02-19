package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Immutable
@Entity
@Table
data class TemplateGroup(
  val code: String,
  val name: String,
  val description: String,
  @Id
  val id: UUID,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return code == (other as TemplateVariable).code
  }

  override fun hashCode(): Int = 31 * code.hashCode()
  override fun toString(): String = """TemplateGroup(code="$code", name="$name")"""
}

interface TemplateGroupRepository : JpaRepository<TemplateGroup, UUID> {
  fun findByCodeIn(code: Set<String>): Set<TemplateGroup>
}
