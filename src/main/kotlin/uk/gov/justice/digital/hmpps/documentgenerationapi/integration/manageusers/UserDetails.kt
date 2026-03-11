package uk.gov.justice.digital.hmpps.documentgenerationapi.integration.manageusers

import com.fasterxml.jackson.annotation.JsonAlias

data class UserDetails(
  val username: String,
  val name: String,
  @JsonAlias("activeCaseLoadId")
  val caseloadId: String?,
)

fun String.asSystemUser() = UserDetails(this, "User $this", null)
