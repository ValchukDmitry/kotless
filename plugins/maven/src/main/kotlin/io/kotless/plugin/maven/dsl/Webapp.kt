package io.kotless.plugin.maven.dsl

import io.kotless.InternalAPI
import io.kotless.dsl.config.KotlessAppConfig
import java.io.Serializable

/**
 * Kotless web application
 * It includes ApiGateway REST API definition and Route53 alias with SSL certificate, if present.
 */
@KotlessDSLTag
class Webapp(configuration: KotlessDSL) : Serializable {
    @KotlessDSLTag
    class Lambda(configuration: KotlessDSL) : Serializable {
        /** Memory in megabytes available for a lambda */
        var memoryMb: Int = 1024

        /** Limit of lambda execution in seconds */
        var timeoutSec: Int = 300

        /** Environment that should be additionally passed to lambda */
        val environment: HashMap<String, String> = HashMap()

        @OptIn(InternalAPI::class)
        internal val mergedEnvironment: Map<String, String>
            get() = environment + mapOf(KotlessAppConfig.PACKAGE_ENV_NAME to kotlessDSL.packages.joinToString(separator = ","))

        @KotlessDSLTag
        class KotlessDSLRuntime(configuration: KotlessDSL) : Serializable {
            /** Default value is the group of project */
            var packages: Set<String> = setOf(configuration.group!!)
        }

        private val kotlessDSL = KotlessDSLRuntime(configuration)

        /** Setup configuration for Kotless DSL */
        @KotlessDSLTag
        fun kotless(configure: KotlessDSLRuntime.() -> Unit) {
            kotlessDSL.configure()
        }
    }

    internal val lambda: Lambda = Lambda(configuration)

    /** Optimizations applied during generation */
    @KotlessDSLTag
    fun lambda(configure: Lambda.() -> Unit) {
        lambda.configure()
    }

    /** Deployment definition of ApiGateway. Recreated each redeploy. */
    @KotlessDSLTag
    class Deployment : Serializable {
        /**
         * A unique name of deployment
         * By default it is `projectName`
         * (in case of unnamed rootProject -- `root`)
         */
        var name: String? = null

        /**
         * Version of this deployment.
         * By default, it is `1`
         */
        var version: String = "1"
    }

    internal val deployment = Deployment()

    /** Deployment resource of ApiGateway */
    @KotlessDSLTag
    fun deployment(configure: Deployment.() -> Unit) {
        deployment.configure()
    }

    /** Alias to RestAPI, if present */
    var route53: Route53? = null

    /**
     * Route53 CNAME alias
     *
     * @param alias name of alias
     * @param zone a qualified name of zone, alias is created in
     * @param certificate a fully qualified name of certificate, for SSL connection
     */
    @KotlessDSLTag
    data class Route53(val alias: String, val zone: String, val certificate: String = "$alias.$zone") : Serializable
}
