package io.kotless.gen.factory.azure.route.dynamic

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.aws.route.AbstractRouteFactory
import io.kotless.gen.factory.azure.info.InfoFactory
import io.kotless.gen.factory.azure.resource.dynamic.FunctionFactory
import io.terraformkt.terraform.TFResource

object DynamicRouteFactory : GenerationFactory<Application.ApiGateway.DynamicRoute, DynamicRouteFactory.Output>, AbstractRouteFactory() {
    data class Output(val fileCreationRef: String)

    override fun mayRun(entity: Application.ApiGateway.DynamicRoute, context: GenerationContext) =
        context.output.check(context.webapp, InfoFactory)

    override fun generate(entity: Application.ApiGateway.DynamicRoute, context: GenerationContext): GenerationFactory.GenerationResult<DynamicRouteFactory.Output> {
        val lambda = context.schema.lambdas[entity.lambda]!!
        val lambdaDescriptionFileBody = """
            {
              "scriptFile" : "../${lambda.file.name}",
              "entryPoint" : "${lambda.entrypoint.qualifiedName}",
              "bindings" : [ {
                "type" : "httpTrigger",
                "direction" : "in",
                "name" : "req",
                "methods" : [ "GET", "POST" ],
                "authLevel" : "ANONYMOUS"
              }, {
                "type" : "http",
                "direction" : "out",
                "name" : "${'$'}return"
              } ]
            }
        """.trimIndent()

        val result = ResourceFromString(entity.path.toString(), "local_file", """
            resource "local_file" "${entity.path}" {
                content     = "${lambdaDescriptionFileBody.replace("\"", "\\\"").replace("\n", "")}"
                filename = "${lambda.file.parent}/${entity.path}/function.json"
            }
        """.trimIndent())

        return GenerationFactory.GenerationResult(Output(result.hcl_ref), result)
    }
}


class ResourceFromString(
    id: String,
    val type: String,
    val value: String
) : TFResource(id, type) {
    override fun render(): String {
        return value
    }
}
