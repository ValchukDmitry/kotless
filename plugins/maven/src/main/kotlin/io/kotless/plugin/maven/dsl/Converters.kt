package io.kotless.plugin.maven.dsl

internal fun Webapp.Route53.toSchema(): io.kotless.Webapp.Route53 = io.kotless.Webapp.Route53(zone, alias, certificate)
internal fun Webapp.Deployment.toSchema(path: String): io.kotless.Webapp.ApiGateway.Deployment = io.kotless.Webapp.ApiGateway.Deployment(
    name ?: path.trim(':').let { if (it.isBlank()) "root" else it.replace(':', '_') },
    version
)
