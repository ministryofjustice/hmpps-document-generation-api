package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.word
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroupTemplates
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateGroups
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateSummary

class GetTemplatesByGroupIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(GET_TEMPLATES_IN_GROUP_URL, word(6))
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    getTemplateGroups(word(6), username(), "ANY_OTHER").expectStatus().isForbidden
  }

  @Test
  fun `200 ok can retrieve template group without any templates`() {
    val grp = givenTemplateGroup(templateGroup(roles = sortedSetOf(word(20))))

    val res = getTemplateGroups(grp.code).successResponse<TemplateGroupTemplates>()

    assertThat(res.group).isEqualTo(TemplateGroups.Group(grp.code, grp.name, grp.description, grp.roles))
    assertThat(res.templates).isEmpty()
  }

  @Test
  fun `200 ok can retrieve template group with templates`() {
    val grp = givenTemplateGroup(templateGroup(roles = sortedSetOf(word(20))))
    val template = givenDocumentTemplate(requiredGroups = setOf(grp.code))

    val res = getTemplateGroups(grp.code).successResponse<TemplateGroupTemplates>()

    assertThat(res.group).isEqualTo(TemplateGroups.Group(grp.code, grp.name, grp.description, grp.roles))
    assertThat(res.templates).containsExactly(TemplateSummary(template.id, template.code, template.name, template.description, template.instructionText, listOf()))
  }

  private fun getTemplateGroups(
    code: String,
    username: String = username(),
    role: String? = listOf(
      Roles.DOCUMENT_GENERATION_UI,
      Roles.DOCUMENT_GENERATION_RO,
      Roles.DOCUMENT_GENERATION_RW,
    ).random(),
  ) = webTestClient
    .get()
    .uri(GET_TEMPLATES_IN_GROUP_URL, code)
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val GET_TEMPLATES_IN_GROUP_URL = "/groups/{groupCode}"
  }
}
