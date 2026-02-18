package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.JdbcType
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Immutable
@Entity
@Table
data class TemplateVariable(
  val key: VariableKey,
  val description: String,
  val sequenceNumber: Int,
  @JdbcType(value = PostgreSQLEnumJdbcType::class)
  val type: Type,
  @Id
  val id: UUID,
) : VariableDomainCode by key {
  enum class Type {
    BINARY,
    DATE,
    STRING,
    TIME,
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as TemplateVariable
    return domain == other.domain && code == other.code
  }

  override fun hashCode(): Int = 31 * domain.hashCode() + code.hashCode()
  override fun toString(): String = """TemplateVariable(domain="$domain", code="$code")"""
}

interface VariableDomainCode {
  val domain: String
  val code: String
}

@Embeddable
data class VariableKey(
  override val domain: String,
  override val code: String,
) : VariableDomainCode

interface TemplateVariableRepository : JpaRepository<TemplateVariable, UUID> {
  fun findByKeyIn(keys: Set<VariableKey>): Set<TemplateVariable>
}
