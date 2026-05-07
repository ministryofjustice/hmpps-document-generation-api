package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import reactor.netty.http.HttpProtocol

@ConfigurationProperties(prefix = "service")
data class ServiceConfig(
  val allowPullingTemplates: Boolean = false,
  val autoPullTemplates: Boolean = false,
  val includeTemplates: Set<String> = setOf(),
  val httpProtocol: Set<HttpProtocol>
)
