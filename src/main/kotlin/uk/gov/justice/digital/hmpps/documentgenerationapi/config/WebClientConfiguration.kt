package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.util.retry.Retry
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import java.io.IOException
import java.time.Duration
import java.time.Duration.ofSeconds

@Configuration
class WebClientConfiguration(
  @Value($$"${integration.document-management.url}") private val documentManagementBaseUri: String,
  @Value($$"${integration.manage-users.url}") private val manageUsersBaseUri: String,
  @Value($$"${integration.template-configuration.url:}") private val templateConfigurationBaseUri: String,
) {

  @Bean
  fun tokenResponseClient(): WebClientReactiveClientCredentialsTokenResponseClient {
    val client = WebClientReactiveClientCredentialsTokenResponseClient()
    val webClient = WebClient.builder().filter { request, next ->
      next.exchange(request)
        .retryWhen(Retry.backoff(3, Duration.ofMillis(50)).filter { it.isRetryableException() })
    }.build()

    client.setWebClient(webClient)
    return client
  }

  private fun Throwable.isRetryableException(): Boolean = this is IOException || this is WebClientRequestException || (this is WebClientResponseException && this.statusCode.is5xxServerError)

  @Bean
  fun documentManagementWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = authorisedWebClient(documentManagementBaseUri, builder, authorizedClientManager)

  @Bean
  fun manageUsersWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = authorisedWebClient(manageUsersBaseUri, builder, authorizedClientManager)

  @Bean
  @ConditionalOnBooleanProperty("service.allow-pulling-templates")
  fun templateConfigurationWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = authorisedWebClient(templateConfigurationBaseUri, builder, authorizedClientManager, registrationId = "template-config", timeout = ofSeconds(3))

  fun authorisedWebClient(
    url: String,
    builder: Builder,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    timeout: Duration = Companion.timeout,
    registrationId: String = DEFAULT_REGISTRATION_ID,
  ): WebClient = builder.authorisedWebClient(authorizedClientManager, registrationId, url, timeout)

  companion object {
    const val DEFAULT_REGISTRATION_ID = "default"
    private val timeout: Duration = ofSeconds(1)
  }
}
