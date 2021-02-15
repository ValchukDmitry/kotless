package io.kotless.gen.factory.azure.route53

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.azure.info.InfoFactory
import io.kotless.gen.factory.azure.resource.dynamic.FunctionFactory
import io.terraformkt.azurerm.resource.app.app_service_custom_hostname_binding
import io.terraformkt.azurerm.resource.dns.dns_cname_record
import io.terraformkt.azurerm.resource.dns.dns_txt_record
import io.terraformkt.hcl.ref

object RecordFactory : GenerationFactory<Application.Route53, RecordFactory.Output> {
    data class Output(val hostnameBinding: String)

    override fun mayRun(entity: Application.Route53, context: GenerationContext) =
        context.output.check(entity, ZoneFactory)
            && context.schema.lambdas.all.all { context.output.check(it, FunctionFactory) }
            && context.output.check(context.webapp, InfoFactory)

    override fun generate(entity: Application.Route53, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val lambda = context.output.get(context.schema.lambdas.all.first(), FunctionFactory)
        val dnsZone = context.output.get(entity, ZoneFactory)
        val resourceGroup = context.output.get(context.webapp, InfoFactory).resourceGroup
        val cnameRecord = dns_cname_record(context.names.tf(entity.zone)) {
            name = entity.alias
            zone_name = dnsZone.zone_name
            resource_group_name = resourceGroup::name.ref
            ttl = 300
//            target_resource_id = lambda.function::id.ref
            record = lambda.function::default_hostname.ref
        }

        val txtRecordAsuid = dns_txt_record("main") {
            name = "asuid.${cnameRecord::name.ref}"
            zone_name = dnsZone.zone_name
            resource_group_name = resourceGroup::name.ref
            ttl = 300
            record {
                value = "\${${lambda.function.hcl_ref}.custom_domain_verification_id}"
            }
        }

        val txtRecordAwverify = dns_txt_record("awverify") {
            name = "awverify"
            zone_name = dnsZone.zone_name
            resource_group_name = resourceGroup::name.ref
            ttl = 300
            record {
                value = "\${${lambda.function.hcl_ref}.custom_domain_verification_id}"
            }
        }

        val hostnameBinding = app_service_custom_hostname_binding(context.names.tf("hostname-binding")) {
            hostname = "\${trim(\"${cnameRecord::fqdn.ref}\", \".\")}"
            app_service_name = lambda.function::name.ref
            resource_group_name = resourceGroup::name.ref
            depends_on = arrayOf(txtRecordAsuid.hcl_ref)
        }

        val integration = app_service_custom_hostname_binding(context.names.tf(entity.alias)) {
            hostname = "${entity.alias}.${entity.zone}"
            app_service_name = lambda.function::name.ref
            resource_group_name = resourceGroup::name.ref
        }


        return GenerationFactory.GenerationResult(Output(""), cnameRecord, hostnameBinding, txtRecordAsuid, txtRecordAwverify, integration)
    }
}
