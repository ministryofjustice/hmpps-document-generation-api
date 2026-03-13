package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.MediaType
import tools.jackson.module.kotlin.jsonMapper
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration.Group
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration.Variable
import java.util.UUID

class TemplateConfigurationServer : WireMockServer(7979) {

  fun stubGetTemplateConfig(config: TemplateConfiguration) {
    stubFor(
      get(urlPathTemplate("/template-configuration/{code}"))
        .withPathParam("code", equalTo(config.code))
        .willReturn(
          aResponse().withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(jsonMapper().writeValueAsString(config)),
        ),
    )
  }

  fun stubDownloadDocument(id: UUID, byteArray: ByteArray) {
    stubFor(
      get(urlPathTemplate("/template-configuration/file/{id}"))
        .withPathParam("id", equalTo(id.toString()))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .withBody(byteArray)
            .withStatus(200),
        ),
    )
  }

  companion object {
    fun templateConfig(
      code: String = word(6),
      name: String = word(12),
      description: String = "Describing $code - $name",
      instructionText: String? = "Instructions for the template",
      externalReference: UUID = newUuid(),
      variables: List<Variable> = emptyList(),
      groups: List<Group> = emptyList(),
      id: UUID = newUuid(),
    ) = TemplateConfiguration(id, code, name, description, instructionText, externalReference, variables, groups)
  }
}

class TemplateConfigurationExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val templateConfigurationApi = TemplateConfigurationServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    templateConfigurationApi.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    templateConfigurationApi.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    templateConfigurationApi.stop()
  }
}
