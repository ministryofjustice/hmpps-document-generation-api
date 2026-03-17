package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.templateconfiguration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.retryOnTransientException
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import java.util.UUID

@Component
@ConditionalOnBooleanProperty("service.allow-pulling-templates")
class TemplateConfigurationClient(
  @Qualifier("templateConfigurationWebClient") private val webClient: WebClient,
) {
  fun getTemplateByCode(code: String): TemplateConfiguration? = webClient.get()
    .uri("/template-configuration/{code}", code)
    .accept(MediaType.APPLICATION_JSON)
    .exchangeToMono { res ->
      when (res.statusCode()) {
        HttpStatus.NOT_FOUND -> Mono.empty()
        HttpStatus.OK -> res.bodyToMono<TemplateConfiguration>()
        else -> res.createError()
      }
    }
    .retryOnTransientException()
    .block()

  fun getTemplate(id: UUID): ByteArray = webClient.get()
    .uri("/template-configuration/file/{id}", id)
    .accept(MediaType.APPLICATION_OCTET_STREAM)
    .retrieve()
    .bodyToMono<ByteArray>()
    .retryOnTransientException()
    .block()!!
}
