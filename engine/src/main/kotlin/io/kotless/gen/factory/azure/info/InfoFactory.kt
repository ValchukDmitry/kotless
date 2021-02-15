package io.kotless.gen.factory.azure.info

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.terraformkt.azurerm.data.resource.resource_group
import io.terraformkt.azurerm.data.resource.ResourceGroup
import io.terraformkt.azurerm.resource.storage.StorageAccount
import io.terraformkt.azurerm.resource.storage.StorageContainer
import io.terraformkt.azurerm.resource.storage.storage_account
import io.terraformkt.azurerm.resource.storage.storage_container
import io.terraformkt.hcl.ref

object InfoFactory : GenerationFactory<Application, InfoFactory.Output> {
    data class Output(val resourceGroup: ResourceGroup, val storageAccount: StorageAccount, val storageContainer: StorageContainer)

    override fun mayRun(entity: Application, context: GenerationContext) = true

    override fun generate(entity: Application, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val resourceGroup = resource_group(context.names.tf(context.schema.config.prefix, "resource_group")) {
            name = context.schema.config.prefix
        }

        val storageAccount = storage_account(context.names.tf(context.schema.config.bucket, "storage_account")) {
            name = "${context.schema.config.prefix.replace("-", "")}storeacc"
            resource_group_name = resourceGroup::name.ref
            location = resourceGroup::location.ref
            account_tier = "Standard"
            account_replication_type = "LRS"
            staticWebsite {
                index_document = "index.html"
            }
        }

        val storageContainer = storage_container("storage_container") {
            name = context.names.azure("contents")
            storage_account_name = storageAccount::name.ref
            container_access_type = "private"
        }


        return GenerationFactory.GenerationResult(Output(resourceGroup, storageAccount, storageContainer), resourceGroup, storageAccount, storageContainer)
    }
}
