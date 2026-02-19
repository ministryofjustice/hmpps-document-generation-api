package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value($$"${integration.hmpps-auth.url}") private val hmppsAuthBaseUri: String,
  @Value($$"${integration.document-management.url}") private val documentManagementBaseUri: String,
  @Value("\${api.health-timeout:1s}") val healthTimeout: Duration,
  @Value("\${api.timeout:1s}") val timeout: Duration,
) {
  // HMPPS Auth health ping is required if your service calls HMPPS Auth to get a token to call other services
  @Bean
  fun hmppsAuthHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun documentManagementHealthWebClient(builder: Builder): WebClient = builder.healthWebClient(documentManagementBaseUri, healthTimeout)

  @Bean
  fun documentManagementWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(authorizedClientManager, DEFAULT_REGISTRATION_ID, documentManagementBaseUri, timeout)

  companion object {
    const val DEFAULT_REGISTRATION_ID = "default"
  }
}
