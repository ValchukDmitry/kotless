package io.kotless.plugin.maven.dsl

import io.kotless.DSLType
import io.kotless.KotlessConfig
import io.kotless.KotlessConfig.Optimization.MergeLambda
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.Serializable

/** Configuration of Kotless itself */
class KotlessDSL : Serializable {

//    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
//    var mavenProject: MavenProject? = null

    @Parameter
    var group: String = ""

    @Parameter
    var bucket: String = ""

    /** Prefix with which all created resources will be prepended */
    @Parameter
    var prefix: String = ""

    /**
     * A local directory Kotless will use to store generated files
     * By default it is `${buildDir}/kotless-gen`
     */
    @Parameter
    var genDirectory = File("test", "kotless-gen")

    @Parameter
    var type: DSLType? = DSLType.Kotless

    @Parameter
    var staticsRoot: File? = null

    @Parameter(name = "groupId")
    private var groupId: String? = null

    internal val deployGenDirectory: File
        get() = File(genDirectory, "deploy")

    internal val localGenDirectory: File
        get() = File(genDirectory, "local")

    val dsl: KotlessConfig.DSL by lazy {
        KotlessConfig.DSL(type!!, staticsRoot!!)
    }

    @Parameter
    lateinit var backend: Backend

    @Parameter
    lateinit var provider: Provider

    val terraform: KotlessConfig.Terraform by lazy {
        KotlessConfig.Terraform(
            "0.11.14",
            KotlessConfig.Terraform.Backend(
                bucket,
                backend.key,
                backend.profile,
                backend.region
            ),
            KotlessConfig.Terraform.AWSProvider(
                provider.version,
                provider.profile,
                provider.region
            ))
    }
    @KotlessDSLTag
    class Optimization : Serializable {
        /**
         * Optimization defines, if different lambdas should be merged into one and when.
         *
         * Basically, lambda serving few endpoints is more likely to be warm.
         *
         * There are 3 levels of merge optimization:
         * * None -- lambdas will never be merged
         * * PerPermissions -- lambdas will be merged, if they have equal permissions
         * * All -- all lambdas in context are merged in one
         */
        var mergeLambda: MergeLambda = MergeLambda.All

        /**
         * Optimization defines, if lambdas should be autowarmed and with what schedule
         *
         * Lambdas cannot be autowarmed with interval more than hour, since it has no practical sense
         */
        @KotlessDSLTag
        data class Autowarm(val enable: Boolean, val minutes: Int) : Serializable

        var autowarm: Autowarm = Autowarm(true, 5)
    }

    internal val optimization: Optimization = Optimization()

    /** Optimizations applied during generation */
    @KotlessDSLTag
    fun optimization(configure: Optimization.() -> Unit) {
        optimization.configure()
    }
}

class Backend : Serializable {
    /**
     * Name of bucket, that will be used as Terraform backend storage
     * By default kotless bucket is used.
     */
    @Parameter
    lateinit var bucket: String

    /**
     * Path in a bucket to store Terraform state
     * By default it is `kotless-state/state.tfstate`
     */
    @Parameter
    var key: String = "kotless-state/state.tfstate"

    @Parameter
    lateinit var profile: String

    @Parameter
    lateinit var region: String
}

class Provider : Serializable {
    @Parameter
    lateinit var version: String
    @Parameter
    lateinit var profile: String
    @Parameter
    lateinit var region: String
}
