package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.IdGenerator.newUuid
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateDetail
import java.util.UUID

class GetTemplateDetailIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(GET_TEMPLATE_DETAIL_URL, newUuid())
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    getTemplate(newUuid(), username(), "ANY_OTHER").expectStatus().isForbidden
  }

  @Test
  fun `200 ok can retrieve template with groups and variables`() {
    val template = givenDocumentTemplate(
      requiredGroups = setOf("EXTERNAL_MOVEMENT", "TEMPORARY_ABSENCE"),
      requiredVariables = mapOf(
        "PERSON__PRISON_NUMBER" to true,
        "PRISON__CODE" to false,
        "TEMPORARY_ABSENCE__START_DATE" to false,
        "TEMPORARY_ABSENCE__END_DATE" to false,
      ),
    )

    val res = getTemplate(template.id).successResponse<TemplateDetail>()
    res.verifyAgainst(template)
    assertThat(res.groups).hasSize(2)
    assertThat(res.groups.map { it.code }).containsExactly("EXTERNAL_MOVEMENT", "TEMPORARY_ABSENCE")
    assertThat(res.variables.domains.map { it.code }).containsExactly("PRISON", "PERSON", "TEMPORARY_ABSENCE")
    res.variables.domains.forEach { domain ->
      when (domain.code) {
        "PERSON" -> assertThat(domain.variables.single().code).isEqualTo("PERSON__PRISON_NUMBER")
        "PRISON" -> assertThat(domain.variables.single().code).isEqualTo("PRISON__CODE")
        else -> assertThat(domain.variables.map { it.code }).containsExactly(
          "TEMPORARY_ABSENCE__START_DATE",
          "TEMPORARY_ABSENCE__END_DATE",
        )
      }
    }
  }

  private fun TemplateDetail.verifyAgainst(template: DocumentTemplate) {
    assertThat(id).isEqualTo(template.id)
    assertThat(code).isEqualTo(template.code)
    assertThat(name).isEqualTo(template.name)
    assertThat(description).isEqualTo(template.description)
  }

  private fun getTemplate(
    id: UUID,
    username: String = username(),
    role: String? = listOf(
      Roles.DOCUMENT_GENERATION_UI,
      Roles.DOCUMENT_GENERATION_RO,
      Roles.DOCUMENT_GENERATION_RW,
    ).random(),
  ) = webTestClient
    .get()
    .uri(GET_TEMPLATE_DETAIL_URL, id)
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val GET_TEMPLATE_DETAIL_URL = "/templates/{id}"
  }
}
