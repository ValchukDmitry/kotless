package io.kotless.gen.factory.azure.route53

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.azure.info.InfoFactory
import io.kotless.gen.factory.aws.infra.ProvidersFactory
import io.terraformkt.azurerm.data.app.app_service_certificate
import io.terraformkt.hcl.ref

object CertificateFactory : GenerationFactory<Application.Route53, Unit> {

    override fun mayRun(entity: Application.Route53, context: GenerationContext) = context.output.check(context.schema.config.terraform, ProvidersFactory)

    override fun generate(entity: Application.Route53, context: GenerationContext): GenerationFactory.GenerationResult<Unit> {
        val resourceGroup = context.output.get(context.webapp, InfoFactory).resourceGroup
        val cert = app_service_certificate(context.names.tf(entity.certificate)) {
            name = entity.fqdn
            resource_group_name = resourceGroup::name.ref
            location = resourceGroup::location.ref

        }

        return GenerationFactory.GenerationResult(Unit, cert)
    }
}
