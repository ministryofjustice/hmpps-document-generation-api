package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.ValidationException
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateContext
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.applies

@Configuration
class DocumentTemplateContextConfiguration(private val contextInterceptor: DocumentTemplateContextInterceptor) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry
      .addInterceptor(contextInterceptor)
      .addPathPatterns("/**")
      .excludePathPatterns(
        "/health/**",
        "/info",
        "/ping",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
      )
  }
}

@Configuration
class DocumentTemplateContextInterceptor : HandlerInterceptor {
  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
  ): Boolean {
    DocumentTemplateContext(getUsername()).applies()
    return true
  }

  override fun afterCompletion(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
    ex: Exception?,
  ) {
    DocumentTemplateContext.clear()
    super.afterCompletion(request, response, handler, ex)
  }

  private fun getUsername(): String = SecurityContextHolder
    .getContext()
    .authentication
    ?.name
    ?.trim()
    ?.takeUnless(String::isBlank)
    ?.also { if (it.length > 64) throw ValidationException("Username must be <= 64 characters") }
    ?: throw ValidationException("Could not find non empty username")
}
