package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.envers.AuditJoinTable
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.exception.NotFoundException
import java.util.Collections.unmodifiableSet
import java.util.UUID

@Audited
@Entity
@Table
class DocumentTemplate(
  code: String,
  name: String,
  description: String,
  instructionText: String?,
  externalReference: UUID = newUuid(),
  @Id
  override val id: UUID = newUuid(),
) : Identifiable {
  @Version
  var version: Int? = null
    private set

  var code: String = code
    private set

  var name: String = name
    private set

  var description: String = description
    private set

  var instructionText: String? = instructionText
    private set

  var externalReference: UUID = externalReference
    private set

  @OneToMany(mappedBy = "template", cascade = [CascadeType.ALL], orphanRemoval = true)
  private var variables: MutableSet<DocumentTemplateVariable> = mutableSetOf()
  fun variables(): Set<DocumentTemplateVariable> = unmodifiableSet(variables)

  @AuditJoinTable(name = "document_template_group_audit")
  @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
  @ManyToMany(cascade = [CascadeType.ALL])
  @JoinTable(
    name = "document_template_group",
    joinColumns = [JoinColumn(name = "template_id")],
    inverseJoinColumns = [JoinColumn(name = "template_group_id")],
  )
  private var groups: MutableSet<TemplateGroup> = mutableSetOf()
  fun groups(): Set<TemplateGroup> = unmodifiableSet(groups)

  fun withNewExternalReference() = apply {
    externalReference = newUuid()
  }

  fun withExternalReference(externalReference: UUID) = apply {
    this.externalReference = externalReference
  }

  fun update(code: String, name: String, description: String, instructionText: String?) = apply {
    this.code = code
    this.name = name
    this.description = description
    this.instructionText = instructionText
  }

  fun withVariables(variables: Set<Pair<TemplateVariable, Boolean>>) = apply {
    val newVariables = variables.associateBy { it.first.code }
    this.variables.removeIf { it.variable.code !in newVariables.keys }
    this.variables.forEach { it.mandatory = requireNotNull(newVariables[it.variable.code]).second }
    val toAdd = variables.filter { it.first.code !in this.variables.map { tv -> tv.variable.code } }
      .map { DocumentTemplateVariable(this, it.first, it.second) }
    this.variables.addAll(toAdd)
  }

  fun withGroups(groups: Set<TemplateGroup>) = apply {
    this.groups.clear()
    this.groups.addAll(groups)
  }
}

interface DocumentTemplateRepository : JpaRepository<DocumentTemplate, UUID> {
  @Query("select tmp from DocumentTemplate tmp join tmp.groups grp where grp.id = :groupId")
  fun findByGroupCode(groupId: UUID): List<DocumentTemplate>

  fun findByCode(code: String): DocumentTemplate?
}

fun DocumentTemplateRepository.getDocumentTemplate(id: UUID) = findByIdOrNull(id) ?: throw NotFoundException(DocumentTemplate::class, id)

fun DocumentTemplateRepository.getByCode(code: String) = findByCode(code) ?: throw NotFoundException(DocumentTemplate::class, code)
