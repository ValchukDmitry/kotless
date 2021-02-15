package io.kotless.gen.factory.azure.resource.static

import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.azure.info.InfoFactory
import io.kotless.resource.StaticResource
import io.kotless.terraform.functions.path
import io.terraformkt.azurerm.resource.storage.storage_blob
import io.terraformkt.hcl.ref

object StaticResourceFactory : GenerationFactory<StaticResource, StaticResourceFactory.Output> {
    data class Output(val storageAccount: String, val storageContainer: String)

    override fun mayRun(entity: StaticResource, context: GenerationContext) = true

    override fun generate(entity: StaticResource, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val storageAccount = context.output.get(context.webapp, InfoFactory).storageAccount
        val storageContainer = context.output.get(context.webapp, InfoFactory).storageContainer

        val storageBlob = storage_blob(context.names.tf(context.schema.config.bucket, entity.path.parts)) {
            name = context.schema.config.bucket
            storage_account_name = storageAccount::name.ref
            storage_container_name = storageContainer::name.ref
            type = "Block"
            source = path(entity.file)
        }

        return GenerationFactory.GenerationResult(Output(storageAccount::name.ref, storageContainer::name.ref), storageAccount, storageContainer, storageBlob)
    }
}
