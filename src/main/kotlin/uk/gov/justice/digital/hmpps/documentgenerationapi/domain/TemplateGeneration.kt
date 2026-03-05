package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.envers.Audited
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.GenerateFromTemplate
import java.util.UUID

@Audited
@Immutable
@Entity
@Table
class DocumentGenerationRequest(
  @ManyToOne(optional = false)
  @JoinColumn(name = "template_id")
  val template: DocumentTemplate,
  @JdbcTypeCode(SqlTypes.JSON)
  val request: GenerateFromTemplate,
  @Id
  override val id: UUID = newUuid(),
) : Identifiable

interface DocumentGenerationRequestRepository : JpaRepository<DocumentGenerationRequest, UUID>
