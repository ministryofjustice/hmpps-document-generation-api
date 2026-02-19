package uk.gov.justice.digital.hmpps.documentgenerationapi.domain

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateContext.Companion.SYSTEM_USERNAME
import java.time.LocalDateTime

data class DocumentTemplateContext(
  val username: String,
  val requestAt: LocalDateTime = LocalDateTime.now(),
) {
  companion object {
    const val SYSTEM_USERNAME = "SYS"

    fun retrieve(): DocumentTemplateContext = DocumentTemplateContextHolder.getContext()
    fun clear() {
      DocumentTemplateContextHolder.clearContext()
    }
  }
}

fun DocumentTemplateContext.applies() = apply { DocumentTemplateContextHolder.setContext(this) }

@Component
class DocumentTemplateContextHolder {
  companion object {
    private var context: ThreadLocal<DocumentTemplateContext> =
      ThreadLocal.withInitial { DocumentTemplateContext(SYSTEM_USERNAME) }

    internal fun getContext(): DocumentTemplateContext = context.get()
    internal fun setContext(emc: DocumentTemplateContext) {
      context.set(emc)
    }

    internal fun clearContext() {
      context.remove()
    }
  }
}
