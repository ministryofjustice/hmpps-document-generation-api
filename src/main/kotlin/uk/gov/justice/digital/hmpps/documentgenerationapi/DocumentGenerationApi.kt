package uk.gov.justice.digital.hmpps.documentgenerationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.documentgenerationapi.config.ServiceConfig

@EnableConfigurationProperties(ServiceConfig::class)
@SpringBootApplication
class DocumentGenerationApi

fun main(args: Array<String>) {
  runApplication<DocumentGenerationApi>(*args)
}
