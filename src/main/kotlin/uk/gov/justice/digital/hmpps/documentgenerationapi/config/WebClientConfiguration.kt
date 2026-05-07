package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.time.Duration.ofSeconds

@Configuration
class WebClientConfiguration(
  private val serviceConfig: ServiceConfig,
  @Value($$"${integration.document-management.url}") private val documentManagementBaseUri: String,
  @Value($$"${integration.manage-users.url}") private val manageUsersBaseUri: String,
  @Value($$"${integration.template-configuration.url:}") private val templateConfigurationBaseUri: String,
) {

  @Bean
  fun documentManagementWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(documentManagementBaseUri, authorizedClientManager)

  @Bean
  fun manageUsersWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(manageUsersBaseUri, authorizedClientManager)

  @Bean
  @ConditionalOnBooleanProperty("service.allow-pulling-templates")
  fun templateConfigurationWebClient(authorizedClientManager: OAuth2AuthorizedClientManager, builder: Builder) = builder.authorisedWebClient(templateConfigurationBaseUri, authorizedClientManager, registrationId = "template-config")

  fun Builder.authorisedWebClient(
    url: String,
    authorizedClientManager: OAuth2AuthorizedClientManager,
    timeout: Duration = Companion.timeout,
    registrationId: String = DEFAULT_REGISTRATION_ID,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager).also {
      it.setDefaultClientRegistrationId(registrationId)
    }

    return baseUrl(url)
      .clientConnector(
        ReactorClientHttpConnector(
          HttpClient.create().protocol(*serviceConfig.httpProtocol.toTypedArray()).responseTimeout(timeout),
        ),
      )
      .filter(oauth2Client)
      .build()
  }

  companion object {
    const val DEFAULT_REGISTRATION_ID = "default"
    private val timeout: Duration = ofSeconds(1)
  }
}
