package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.exception.NotFoundException
import java.lang.classfile.Attributes.code
import java.util.SortedSet
import java.util.UUID

@Immutable
@Entity
@Table
data class TemplateGroup(
  val code: String,
  val name: String,
  val description: String,
  @JdbcTypeCode(SqlTypes.ARRAY)
  val roles: SortedSet<String>,
  @Id
  val id: UUID = newUuid(),
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
  fun findByCode(code: String): TemplateGroup?
  fun findByCodeIn(code: Set<String>): Set<TemplateGroup>
}

fun TemplateGroupRepository.getGroup(code: String): TemplateGroup = findByCode(code) ?: throw NotFoundException(TemplateGroup::class, code)
