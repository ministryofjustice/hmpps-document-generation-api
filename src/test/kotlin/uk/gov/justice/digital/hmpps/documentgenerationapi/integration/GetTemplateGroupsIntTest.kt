package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups

class GetTemplateGroupsIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(GET_TEMPLATE_GROUPS_URL)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    getTemplateGroups(username(), "ANY_OTHER").expectStatus().isForbidden
  }

  @Test
  fun `200 ok can retrieve variables grouped by domain`() {
    val res = getTemplateGroups().successResponse<TemplateGroups>()

    assertThat(res).isEqualTo(
      TemplateGroups(
        listOf(
          TemplateGroups.Group(
            code = "EXTERNAL_MOVEMENT",
            name = "External movement templates",
            description = "Document templates associated with external movements in general. These require a person to be selected",
          ),
          TemplateGroups.Group(
            code = "TEMPORARY_ABSENCE",
            name = "Temporary absence templates",
            description = "Document templates associated with temporary absences. These require a person and a temporary absence to be selected",
          ),
        ),
      ),
    )
  }

  private fun getTemplateGroups(
    username: String = username(),
    role: String? = Roles.DOCUMENT_GENERATION_UI,
  ) = webTestClient
    .get()
    .uri(GET_TEMPLATE_GROUPS_URL)
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val GET_TEMPLATE_GROUPS_URL = "/groups"
  }
}
