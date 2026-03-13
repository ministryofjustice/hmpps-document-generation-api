package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.templateconfiguration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.retryOnTransientException
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import java.util.UUID

@Component
@ConditionalOnBooleanProperty("service.auto-configure-templates")
class TemplateConfigurationClient(
  @Qualifier("templateConfigurationWebClient") private val webClient: WebClient,
) {
  fun getTemplateByCode(code: String): TemplateConfiguration = webClient.get()
    .uri("/template-configuration/{code}", code)
    .accept(MediaType.APPLICATION_JSON)
    .retrieve()
    .bodyToMono<TemplateConfiguration>()
    .retryOnTransientException()
    .block()!!

  fun getTemplate(id: UUID): ByteArray = webClient.get()
    .uri("/template-configuration/file/{id}", id)
    .accept(MediaType.APPLICATION_OCTET_STREAM)
    .retrieve()
    .bodyToMono<ByteArray>()
    .retryOnTransientException()
    .block()!!
}
