package io.kotless.gen.factory.azure

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.azure.route.static.StaticRouteFactory
import io.kotless.gen.factory.azure.route.dynamic.DynamicRouteFactory
import io.terraformkt.terraform.TFResource

object ZipArchiveFactory : GenerationFactory<Application, ZipArchiveFactory.Output> {
    data class Output(val artifactCompleteRef: String)

    override fun mayRun(entity: Application, context: GenerationContext) =
        context.webapp.api.dynamics.all { context.output.check(it, DynamicRouteFactory) }
            && context.webapp.api.statics.all { context.output.check(it, StaticRouteFactory) }

    override fun generate(entity: Application, context: GenerationContext): GenerationFactory.GenerationResult<ZipArchiveFactory.Output> {
        val lambdas = context.schema.lambdas.all
        val directory = lambdas.first().file.parentFile
        val createFile = entity.api.dynamics.map { context.output.get(it, DynamicRouteFactory).fileCreationRef }

        val hostJson = """
            {
              "version": "2.0",
              "extensionBundle": {
                "id": "Microsoft.Azure.Functions.ExtensionBundle",
                "version": "[1.*, 2.0.0)"
              },
              "extensions": {
                "http": {
                  "routePrefix": ""
                }
              }
            }
        """.trimIndent().replace("\"", "\\\"").replace("\n", "")

        val localSettingsJson = """
            {
              "IsEncrypted": false,
              "Values": {
                "AzureWebJobsStorage": "",
                "FUNCTIONS_WORKER_RUNTIME": "java"
              }
            }
        """.trimIndent().replace("\"", "\\\"").replace("\n", "")

        val createLocalSettingsFile = io.kotless.gen.factory.azure.resource.dynamic.ResourceFromString("local_settings_file", "local_file", """
            resource "local_file" "local_settings_file" {
                content     = "$localSettingsJson"
                filename = "${directory}/local.settings.json"
            }
        """.trimIndent())

        val createHostFile = io.kotless.gen.factory.azure.resource.dynamic.ResourceFromString("host_file", "local_file", """
            resource "local_file" "host_file" {
                content     = "$hostJson"
                filename = "${directory}/host.json"
            }
        """.trimIndent())


        val zipFile = ResourceFromString("zip_file", "archive_file", """
            resource "archive_file" "zip_file" {
                type        = "zip"
                output_path = "${directory.parent}/result.zip"
                source_dir = "$directory"

                depends_on = [${createFile.joinToString("\",\"", prefix = "\"", postfix = "\"")}, "${createHostFile.hcl_ref}", "${createLocalSettingsFile.hcl_ref}"]
            }
        """.trimIndent())

        return GenerationFactory.GenerationResult(Output(zipFile.hcl_ref), zipFile, createLocalSettingsFile, createHostFile)
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
