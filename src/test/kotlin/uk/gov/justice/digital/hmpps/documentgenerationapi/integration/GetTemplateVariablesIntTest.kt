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
              TemplateVariables.Variable(code = "prsnCode", description = "Prison code", type = Type.STRING),
              TemplateVariables.Variable(code = "prsnName", description = "Prison name", type = Type.STRING),
              TemplateVariables.Variable(code = "prsnAddress", description = "Prison address", type = Type.STRING),
              TemplateVariables.Variable(code = "prsnPhone", description = "Prison phone number", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "PERSON",
            description = "Prisoner details",
            variables = listOf(
              TemplateVariables.Variable(code = "perName", description = "Full name", type = Type.STRING),
              TemplateVariables.Variable(code = "perFirstName", description = "First name", type = Type.STRING),
              TemplateVariables.Variable(code = "perMiddleNames", description = "Middle names", type = Type.STRING),
              TemplateVariables.Variable(code = "perLastName", description = "Last name", type = Type.STRING),
              TemplateVariables.Variable(code = "perImage", description = "Prisoner photo", type = Type.BINARY),
              TemplateVariables.Variable(code = "perPrsnNo", description = "Prison number", type = Type.STRING),
              TemplateVariables.Variable(code = "perCro", description = "CRO number", type = Type.STRING),
              TemplateVariables.Variable(code = "perPnc", description = "PNC number", type = Type.STRING),
              TemplateVariables.Variable(code = "perBookNo", description = "Booking number", type = Type.STRING),
              TemplateVariables.Variable(code = "perDob", description = "Date of birth", type = Type.DATE),
              TemplateVariables.Variable(code = "perSecCat", description = "Security category", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "SENTENCE",
            description = "Sentence details",
            variables = listOf(
              TemplateVariables.Variable(code = "sentMainOff", description = "Main offence", type = Type.STRING),
              TemplateVariables.Variable(code = "sentCurOff", description = "Current offence", type = Type.STRING),
              TemplateVariables.Variable(
                code = "sentEarliestCrtApp",
                description = "Earliest court appearance",
                type = Type.DATE,
              ),
              TemplateVariables.Variable(
                code = "sentDate",
                description = "Date of sentence",
                type = Type.DATE,
              ),
              TemplateVariables.Variable(
                code = "sentLenYears",
                description = "Sentence length - years",
                type = Type.STRING,
              ),
              TemplateVariables.Variable(
                code = "sentLenMonths",
                description = "Sentence length - months",
                type = Type.STRING,
              ),
              TemplateVariables.Variable(
                code = "sentLenDays",
                description = "Sentence length - days",
                type = Type.STRING,
              ),
              TemplateVariables.Variable(code = "sentArdCrd", description = "ARD/CRD", type = Type.STRING),
              TemplateVariables.Variable(code = "sentCrd", description = "CRD", type = Type.STRING),
              TemplateVariables.Variable(code = "sentPed", description = "PED or review date", type = Type.STRING),
              TemplateVariables.Variable(code = "sentSled", description = "SLED", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "TEMPORARY_ABSENCE",
            description = "Absence information",
            variables = listOf(
              TemplateVariables.Variable(code = "tapStartDate", description = "Start date", type = Type.DATE),
              TemplateVariables.Variable(code = "tapStartTime", description = "Start time", type = Type.TIME),
              TemplateVariables.Variable(code = "tapEndDate", description = "Expiry date", type = Type.DATE),
              TemplateVariables.Variable(code = "tapEndTime", description = "Expiry time", type = Type.TIME),
              TemplateVariables.Variable(code = "tapCat", description = "Reason for absence", type = Type.STRING),
            ),
          ),
          TemplateVariables.Domain(
            code = "OFFENDER_MANAGER",
            description = "Manager details",
            variables = listOf(
              TemplateVariables.Variable(
                code = "omApptDate",
                description = "Date of initial appointment with offender manager after release on licence",
                type = Type.DATE,
              ),
              TemplateVariables.Variable(code = "omApptTime", description = "Start time", type = Type.TIME),
              TemplateVariables.Variable(
                code = "omApptAddr",
                description = "Address of probation office where initial appointment is scheduled",
                type = Type.STRING,
              ),
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
