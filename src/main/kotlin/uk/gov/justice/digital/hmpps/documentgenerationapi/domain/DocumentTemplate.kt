package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.envers.Audited
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import java.util.Collections.unmodifiableSet
import java.util.UUID

@Audited
@Entity
@Table
class DocumentTemplate(
  val code: String,
  name: String,
  description: String,
  variables: Set<DocumentTemplateVariable>,
  externalReference: UUID = newUuid(),
  @Id
  val id: UUID = newUuid(),
) {
  @Version
  var version: Int? = null
    private set

  var name: String = name
    private set

  var description: String = description
    private set

  var externalReference: UUID = externalReference
    private set

  @OneToMany(mappedBy = "template", cascade = [CascadeType.ALL], orphanRemoval = true)
  private var variables: MutableSet<DocumentTemplateVariable> = mutableSetOf()
  fun variables(): Set<DocumentTemplateVariable> = unmodifiableSet(variables)

  init {
    this.variables.addAll(variables)
  }

  fun withNewExternalReference() = apply {
    externalReference = newUuid()
  }

  fun update(name: String, description: String) = apply {
    this.name = name
    this.description = description
  }

  fun withVariables(variables: Set<Pair<TemplateVariable, Boolean>>) = apply {
    val newVariables = variables.associateBy { it.first.code }
    this.variables.removeIf { it.variable.code !in newVariables.keys }
    this.variables.forEach { it.mandatory = requireNotNull(newVariables[it.variable.code]).second }
    val toAdd = variables.filter { it.first.code !in this.variables.map { tv -> tv.variable.code } }
      .map { DocumentTemplateVariable(this, it.first, it.second) }
    this.variables.addAll(toAdd)
  }
}

interface DocumentTemplateRepository : JpaRepository<DocumentTemplate, UUID> {
  fun findByCode(code: String): DocumentTemplate?
}
