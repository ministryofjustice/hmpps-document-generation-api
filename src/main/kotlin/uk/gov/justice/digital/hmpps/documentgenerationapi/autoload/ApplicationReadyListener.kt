package uk.gov.justice.digital.hmpps.documentgenerationapi.autoload

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnBooleanProperty("service.auto-pull-templates", matchIfMissing = false)
class ApplicationReadyListener(private val autoLoadTemplates: AutoLoadTemplates) {
  @EventListener
  fun onApplicationReady(event: ApplicationReadyEvent) {
    autoLoadTemplates.refreshAllTemplates()
  }
}
