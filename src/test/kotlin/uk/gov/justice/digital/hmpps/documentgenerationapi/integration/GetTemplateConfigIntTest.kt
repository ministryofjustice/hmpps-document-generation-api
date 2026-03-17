package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateConfiguration

class GetTemplateConfigIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(GET_TEMPLATE_CONFIG_URL, word(8))
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    getTemplateConfig(word(8), username(), "ANY_OTHER").expectStatus().isForbidden
  }

  @Test
  fun `200 ok can retrieve template with groups and variables`() {
    val template = givenDocumentTemplate(
      requiredGroups = setOf("EXTERNAL_MOVEMENT", "TEMPORARY_ABSENCE"),
      requiredVariables = mapOf(
        "perPrsnNo" to true,
        "prsnCode" to false,
        "tapStartDate" to false,
        "tapEndDate" to false,
      ),
    )

    val res = getTemplateConfig(template.code).successResponse<TemplateConfiguration>()
    res.verifyAgainst(template)
    assertThat(res.groups).hasSize(2)
    assertThat(res.groups.map { it.code }).containsExactlyInAnyOrder("EXTERNAL_MOVEMENT", "TEMPORARY_ABSENCE")
    assertThat(res.variables.map { it.code }).containsExactlyInAnyOrder(
      "perPrsnNo",
      "prsnCode",
      "tapStartDate",
      "tapEndDate",
    )
  }

  private fun TemplateConfiguration.verifyAgainst(template: DocumentTemplate) {
    assertThat(id).isEqualTo(template.id)
    assertThat(code).isEqualTo(template.code)
    assertThat(name).isEqualTo(template.name)
    assertThat(description).isEqualTo(template.description)
    assertThat(instructionText).isEqualTo(template.instructionText)
    assertThat(externalReference).isEqualTo(template.externalReference)
  }

  private fun getTemplateConfig(
    code: String,
    username: String = username(),
    role: String? = Roles.TEMPLATE_CONFIGURATION_RW,
  ) = webTestClient
    .get()
    .uri(GET_TEMPLATE_CONFIG_URL, code)
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val GET_TEMPLATE_CONFIG_URL = "/template-configuration/{code}"
  }
}
