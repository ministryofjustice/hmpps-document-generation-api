package uk.gov.justice.digital.hmpps.documentgenerationapi.integration

object DataGenerator {
  private val letters = ('A'..'Z')
  fun word(length: Int): String = (1..length).joinToString("") { if (it == 1) letters.random().uppercase() else letters.random().lowercase() }

  fun username(): String = (0..12).joinToString("") { letters.random().toString() }
}
