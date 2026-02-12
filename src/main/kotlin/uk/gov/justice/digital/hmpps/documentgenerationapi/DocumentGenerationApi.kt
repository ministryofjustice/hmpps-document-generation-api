package uk.gov.justice.digital.hmpps.documentgenerationapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DocumentGenerationApi

fun main(args: Array<String>) {
  runApplication<DocumentGenerationApi>(*args)
}
