package uk.gov.justice.digital.hmpps.documentgenerationapi.domain.exception

import kotlin.reflect.KClass

class NotFoundException(val entity: String, val identifier: Any) : RuntimeException(entity) {
  constructor(entity: KClass<out Any>, identifier: Any) : this(entity::class.simpleName!!, identifier)
}
