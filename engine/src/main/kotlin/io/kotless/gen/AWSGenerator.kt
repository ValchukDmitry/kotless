package io.kotless.gen

import io.kotless.*
import io.kotless.gen.factory.aws.apigateway.DeploymentFactory
import io.kotless.gen.factory.aws.apigateway.DomainFactory
import io.kotless.gen.factory.aws.apigateway.RestAPIFactory
import io.kotless.gen.factory.aws.event.ScheduledEventsFactory
import io.kotless.gen.factory.aws.info.InfoFactory
import io.kotless.gen.factory.aws.infra.TFConfigFactory
import io.kotless.gen.factory.aws.infra.ProvidersFactory
import io.kotless.gen.factory.aws.resource.dynamic.LambdaFactory
import io.kotless.gen.factory.aws.resource.static.StaticResourceFactory
import io.kotless.gen.factory.aws.route.dynamic.DynamicRouteFactory
import io.kotless.gen.factory.aws.route.static.StaticRoleFactory
import io.kotless.gen.factory.aws.route.static.StaticRouteFactory
import io.kotless.gen.factory.aws.route53.CertificateFactory
import io.kotless.gen.factory.aws.route53.RecordFactory
import io.kotless.gen.factory.aws.route53.ZoneFactory
import io.kotless.resource.Lambda
import io.kotless.resource.StaticResource
import io.terraformkt.terraform.TFFile
import kotlin.reflect.KClass

object AWSGenerator {
    private val factories: Map<KClass<*>, Set<GenerationFactory<*, *>>> = mapOf(
        Application::class to setOf(InfoFactory, StaticRoleFactory),
        KotlessConfig.Terraform::class to setOf(TFConfigFactory, ProvidersFactory),

        Application.ApiGateway::class to setOf(DomainFactory, RestAPIFactory),
        Application.ApiGateway.Deployment::class to setOf(DeploymentFactory),
        Application.Route53::class to setOf(CertificateFactory, RecordFactory, ZoneFactory),

        StaticResource::class to setOf(StaticResourceFactory),
        Lambda::class to setOf(LambdaFactory),

        Application.Events.Scheduled::class to setOf(ScheduledEventsFactory),

        Application.ApiGateway.StaticRoute::class to setOf(StaticRouteFactory),
        Application.ApiGateway.DynamicRoute::class to setOf(DynamicRouteFactory)
    )

    fun generate(schema: Schema): Set<TFFile> {
        val context = GenerationContext(schema, schema.application)

        var newExecuted = true
        while (newExecuted) {
            newExecuted = false
            schema.visit { entity ->
                factories[entity::class].orEmpty().forEach { factory ->
                    @Suppress("UNCHECKED_CAST")
                    factory as GenerationFactory<Any, Any>
                    if (!factory.hasRan(entity, context)) {
                        if (factory.mayRun(entity, context)) {
                            factory.run(entity, context)
                            newExecuted = true
                        }
                    }
                }
            }
        }

        return setOf(TFFile(context.webapp.api.name, ArrayList(context.entities.all())))
    }
}
