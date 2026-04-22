package uk.gov.justice.digital.hmpps.documentgenerationapi.config

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.SpelEvaluationException
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.method.HandlerMethod

const val TEMPLATES = "Templates"
const val TEMPLATE_VARIABLES = "Template variables"
const val TEMPLATE_GROUPS = "Template groups"

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties, private val context: ApplicationContext) {
  private val version: String? = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://document-generation-api-dev.hmpps.service.justice.gov.uk").description("Development"),
        Server().url("https://document-generation-api-preprod.hmpps.service.justice.gov.uk")
          .description("Pre-Production"),
        Server().url("https://document-generation-api.hmpps.service.justice.gov.uk").description("Production"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .tags(
      listOf(
        Tag().name(TEMPLATES).description("Create and retrieve document templates, and generate documents from them"),
        Tag().name(TEMPLATE_VARIABLES).description("Retrieve the supported template variables and their domains"),
        Tag().name(TEMPLATE_GROUPS).description("Retrieve template groups and the templates within them"),
      ),
    )
    .info(
      Info()
        .title("HMPPS Document Generation API")
        .version(version)
        .description(
          "A single-purpose document generation API. Retrieves Word document templates from the HMPPS Document Management API, " +
            "applies bookmark based variable substitution via docx4j, and returns the generated document to the caller.\n\n" +
            "Designed as a shared DPS capability with no knowledge of any specific HMPPS domain. " +
            "Currently used exclusively by the Receptions and External Movements product set.\n\n" +
            "## Design constraints\n\n" +
            "* No document storage - documents are generated on demand and returned to the caller\n" +
            "* No NOMIS dependency - template variables are supplied by the caller\n" +
            "* No domain logic - the API has no knowledge of what the documents are for\n\n" +
            "## Authentication\n\n" +
            "This API uses OAuth2 with JWTs. Pass the JWT in the `Authorization` header using the `Bearer` scheme.\n\n" +
            "## Authorisation\n\n" +
            "Roles required for each endpoint are documented in the endpoint descriptions.",
        )
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    ).components(
      Components().addSecuritySchemes(
        "bearer-jwt",
        SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
          .`in`(SecurityScheme.In.HEADER)
          .name("Authorization"),
      ),
    )
    .addSecurityItem(SecurityRequirement().addList("bearer-jwt", listOf("read", "write")))
    .also { PrimitiveType.enablePartialTime() }

  @Bean
  fun preAuthorizeCustomizer(): OperationCustomizer = OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
    handlerMethod.preAuthorizeForMethodOrClass()?.let {
      val preAuthExp = SpelExpressionParser().parseExpression(it)
      val evalContext = StandardEvaluationContext()
      evalContext.beanResolver = BeanFactoryResolver(context)
      evalContext.setRootObject(
        object {
          fun hasRole(role: String) = listOf(role)
          fun hasAnyRole(vararg roles: String) = roles.toList()
        },
      )

      val roles = try {
        (preAuthExp.getValue(evalContext) as List<*>).filterIsInstance<String>()
      } catch (e: SpelEvaluationException) {
        emptyList()
      }
      if (roles.isNotEmpty()) {
        operation.description = "${operation.description ?: ""}\n\n" +
          "Requires one of the following roles:\n" +
          roles.joinToString(prefix = "* ", separator = "\n* ")
      }
    }

    operation
  }

  private fun HandlerMethod.preAuthorizeForMethodOrClass() = getMethodAnnotation(PreAuthorize::class.java)?.value
    ?: beanType.getAnnotation(PreAuthorize::class.java)?.value
}
