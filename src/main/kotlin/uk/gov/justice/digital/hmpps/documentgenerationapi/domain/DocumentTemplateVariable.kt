package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import java.util.UUID

@Audited
@Entity
@Table
class DocumentTemplateVariable(

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "template_id", nullable = false)
  val template: DocumentTemplate,

  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "variable_id", nullable = false)
  val variable: TemplateVariable,

  internal var mandatory: Boolean,

  @Id
  val id: UUID = newUuid(),
)
