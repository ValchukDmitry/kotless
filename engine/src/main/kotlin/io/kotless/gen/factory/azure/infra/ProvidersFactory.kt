package io.kotless.gen.factory.azure.infra

import io.kotless.KotlessConfig
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.terraformkt.azurerm.provider.Provider
import io.terraformkt.azurerm.provider.provider

object ProvidersFactory : GenerationFactory<KotlessConfig.Terraform, ProvidersFactory.Output> {
    class Output(val provider: Provider)

    override fun mayRun(entity: KotlessConfig.Terraform, context: GenerationContext) = true

    override fun generate(entity: KotlessConfig.Terraform, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val provider = provider() {
            features { }
        }

        return GenerationFactory.GenerationResult(Output(provider), provider)
    }
}
