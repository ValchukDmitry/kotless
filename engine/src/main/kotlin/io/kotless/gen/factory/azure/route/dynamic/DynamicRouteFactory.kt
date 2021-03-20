package io.kotless.gen.factory.azure.route.dynamic

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.aws.route.AbstractRouteFactory
import io.kotless.gen.factory.azure.filescontent.LambdaDescription
import io.kotless.gen.factory.azure.info.InfoFactory
import io.kotless.gen.factory.azure.utils.FilesCreationTf

object DynamicRouteFactory : GenerationFactory<Application.ApiGateway.DynamicRoute, DynamicRouteFactory.Output>, AbstractRouteFactory() {
    data class Output(val fileCreationRef: String, val proxyPart: String)

    override fun mayRun(entity: Application.ApiGateway.DynamicRoute, context: GenerationContext) =
        context.output.check(context.webapp, InfoFactory)

    override fun generate(
        entity: Application.ApiGateway.DynamicRoute,
        context: GenerationContext
    ): GenerationFactory.GenerationResult<Output> {
        val lambda = context.schema.lambdas[entity.lambda]!!
        val functionAppName = context.names.azure(lambda.name)
        val lambdaDescriptionFileBody = LambdaDescription.body(lambda)

        val resourceName = "route_${entity.path.toString().replace(".", "_").replace("/", "_")}"
        val path = "route_" + entity.path.toString().replace("/", "_")

        val result = FilesCreationTf.localFile(resourceName, lambdaDescriptionFileBody, "${lambda.file.parent}/$path/function.json")
        val proxyPart = LambdaDescription.proxy(path, entity, functionAppName)

        return GenerationFactory.GenerationResult(Output(result.hcl_ref, proxyPart), result)
    }
}
