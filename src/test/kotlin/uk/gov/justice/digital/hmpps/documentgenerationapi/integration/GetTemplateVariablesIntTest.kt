package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.documentgenerationapi.Roles
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.TemplateVariable.Type
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.DataGenerator.username
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.TemplateVariables

class GetTemplateVariablesIntTest : IntegrationTestBase() {

  @Test
  fun `401 unauthorised without a valid token`() {
    webTestClient
      .put()
      .uri(GET_TEMPLATE_VARIABLES_URL)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `403 forbidden without correct role`() {
    getTemplateVariables(username(), "ANY_OTHER").expectStatus().isForbidden
  }

  @Test
  fun `200 ok can retrieve variables grouped by domain`() {
    val res = getTemplateVariables().successResponse<TemplateVariables>()

    assertThat(res).isEqualTo(
      TemplateVariables(
        domains = listOf(
          TemplateVariables.Domain(
            code = "PRISON",
            description = "Prison details",
            variables = listOf(
              TemplateVariables.Variable(code = "PRISON__CODE", description = "Prison code", type = Type.STRING),
              TemplateVariables.Variable(code = "PRISON__NAME", description = "Prison name", type = Type.STRING),
              TemplateVariables.Variable(code = "PRISON__ADDRESS", description = "Prison address", type = Type.STRING),
              TemplateVariables.Variable(code = "PRISON__PHONE", description = "Prison phone number", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "PERSON",
            description = "Prisoner details",
            variables = listOf(
              TemplateVariables.Variable(code = "PERSON__NAME", description = "Full name", type = Type.STRING),
              TemplateVariables.Variable(code = "PERSON__IMAGE", description = "Prisoner photo", type = Type.BINARY),
              TemplateVariables.Variable(code = "PERSON__PRISON_NUMBER", description = "Prison number", type = Type.STRING),
              TemplateVariables.Variable(code = "PERSON__COURT_REFERENCE_NUMBER", description = "CRO number", type = Type.STRING),
              TemplateVariables.Variable(code = "PERSON__POLICE_NATIONAL_COMPUTER_NUMBER", description = "PNC number", type = Type.STRING),
              TemplateVariables.Variable(code = "PERSON__BOOKING_NUMBER", description = "Booking number", type = Type.STRING),
              TemplateVariables.Variable(code = "PERSON__DATE_OF_BIRTH", description = "Date of birth", type = Type.DATE),
            ),
          ),
          TemplateVariables.Domain(
            code = "TEMPORARY_ABSENCE",
            description = "Absence information",
            variables = listOf(
              TemplateVariables.Variable(code = "TEMPORARY_ABSENCE__START_DATE", description = "Start date", type = Type.DATE),
              TemplateVariables.Variable(code = "TEMPORARY_ABSENCE__START_TIME", description = "Start time", type = Type.TIME),
              TemplateVariables.Variable(code = "TEMPORARY_ABSENCE__END_DATE", description = "Expiry date", type = Type.DATE),
              TemplateVariables.Variable(code = "TEMPORARY_ABSENCE__END_TIME", description = "Expiry time", type = Type.TIME),
              TemplateVariables.Variable(code = "TEMPORARY_ABSENCE__CATEGORISATION", description = "Reason for absence", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "OFFENDER_MANAGER",
            description = "Manager details",
            variables = listOf(
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__NAME", description = "Offender manager name", type = Type.STRING),
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__ROLE", description = "Offender manager role", type = Type.STRING),
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__PHONE", description = "Offender manager phone number", type = Type.STRING),
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__INITIAL_APPOINTMENT_DATE", description = "Date of initial appointment with offender manager after release on licence", type = Type.DATE),
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__INITIAL_APPOINTMENT_TIME", description = "Start time", type = Type.TIME),
              TemplateVariables.Variable(code = "OFFENDER_MANAGER__INITIAL_APPOINTMENT_ADDRESS", description = "Address of probation office where initial appointment is scheduled", type = Type.STRING),
            ),
          ),
        ),
      ),
    )
  }

  private fun getTemplateVariables(
    username: String = username(),
    role: String? = Roles.DOCUMENT_GENERATION_UI,
  ) = webTestClient
    .get()
    .uri(GET_TEMPLATE_VARIABLES_URL)
    .headers(setAuthorisation(username = username, roles = listOfNotNull(role)))
    .exchange()

  companion object {
    const val GET_TEMPLATE_VARIABLES_URL = "/variables"
  }
}
