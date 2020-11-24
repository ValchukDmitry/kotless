package io.kotless.plugin.maven.tasks.gen

import io.kotless.*
import io.kotless.parser.KotlessParser
import io.kotless.parser.ktor.KTorParser
import io.kotless.parser.spring.SpringParser
import io.kotless.plugin.maven.dsl.KotlessDSL
import io.kotless.plugin.maven.dsl.toSchema
import io.kotless.plugin.maven.utils.clearDirectory
import io.kotless.terraform.TFFile
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.util.FileUtils
import java.io.File

/**
 * KotlessGenerate task generates terraform code from Kotlin code written with Kotless.
 *
 * It takes all the configuration from global KotlessDSL configuration (at `kotless` field)
 *
 * @see kotless
 *
 * <pre>
 * executeMojo(
 *              plugin(
 *                      groupId("org.apache.maven.plugins"),
 *                      artifactId("maven-shade-plugin"),
 *                      version("3.2.4")
 *              ),
 *              goal("shade"),
 *              configuration(),
 *              executionEnvironment(
 *                      project,
 *                      session,
 *                      pluginManager
 *              )
 *          );
 * </pre>
 * @see http://code.google.com/p/mojo-executor/
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
class KotlessGenerateTask : AbstractMojo() {
    @Parameter
    lateinit var kotless: KotlessDSL

    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    var project: MavenProject? = null

    @Parameter
    var sourceDirs: Set<String>? = null

    private val myAllResources: Set<File> by lazy { project!!.resources.flatMap { files(setOf(it.directory)) }.toSet() }

    private val dependencies: Set<File> by lazy { project!!.artifacts.mapNotNull { it.file }.toSet() }

    @Parameter
    var myTerraformAdditional: List<File> = emptyList()

    val myGenDirectory: File
        get() = File("kotless-gen")

    override fun execute() {
        log.info("Task execution has been started")
        myGenDirectory.clearDirectory()
        log.info("Directory has been cleared")

        log.info("Schema parsing has been started")
        val schema = parseSources()
        log.info("Schema parsing has been finished")

        log.info("Kotless generation has been started")
        val generated = KotlessEngine.generate(schema)
        log.info("Kotless generation has been finished")
        dumpGeneratedFiles(generated)
    }


    private fun parseSources(): Schema {
        val config = KotlessConfig(kotless.bucket, kotless.prefix, kotless.dsl, kotless.terraform)
        val myWebapp = io.kotless.plugin.maven.dsl.Webapp(kotless)
        val lambda = Lambda.Config(myWebapp.lambda.memoryMb, myWebapp.lambda.timeoutSec, myWebapp.lambda.mergedEnvironment)

        val jar = File(project!!.build.directory).resolve("test-1.0-SNAPSHOT.jar")
        val sources = files(sourceDirs!!)

        val artifact = project!!.artifact

        val parsed = when (config.dsl.type) {
            DSLType.Kotless -> KotlessParser.parse(sources, myAllResources, jar, config, lambda, dependencies)
            DSLType.Ktor -> KTorParser.parse(sources, myAllResources, jar, config, lambda, dependencies)
            DSLType.SpringBoot -> SpringParser.parse(sources, myAllResources, jar, config, lambda, dependencies)
        }

        val webapp = Webapp(
            route53 = myWebapp.route53?.toSchema(),
            api = Webapp.ApiGateway(
                name = project!!.name,
                deployment = myWebapp.deployment.toSchema("${artifact.groupId}:${artifact.artifactId}:${artifact.version}"),
                dynamics = parsed.routes.dynamics,
                statics = parsed.routes.statics
            ),
            events = Webapp.Events(parsed.events.scheduled)
        )

        return Schema(
            config = config,
            webapp = webapp,
            lambdas = parsed.resources.dynamics,
            statics = parsed.resources.statics
        )
    }

    private fun files(sources: Set<String>): Set<File> {
        return sources.map { File(it).walk().filter { it.isFile }.toList() }.flatten().toSet()
    }

    private fun dumpGeneratedFiles(generated: Set<TFFile>) {
        val files = KotlessEngine.dump(myGenDirectory, generated)
        for (file in myTerraformAdditional) {
            require(files.all { it.name != file.name }) { "Extending terraform file `${file.absolutePath}` clashes with generated file" }
            FileUtils.copyFile(file, File(myGenDirectory, file.name))
        }
    }
}
