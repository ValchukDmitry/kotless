package io.kotless.plugin.gradle.tasks.terraform

import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.utils.*
import io.kotless.plugin.gradle.utils.CommandLine.os
import io.kotless.plugin.gradle.utils.gradle.Groups
import org.codehaus.plexus.util.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import java.io.File
import java.net.URL

/**
 * TerraformDownload task downloads terraform binary needed by Kotless.
 *
 * It takes all the configuration from global KotlessDSL configuration (from `kotless` field)
 * and more precisely -- version from it's terraform field.
 *
 * @see kotless
 *
 * Note: OS for binary is determined automatically and assumed to be `amd64`.
 */
internal open class TerraformDownloadTask : DefaultTask() {
    init {
        group = Groups.setup
    }

    companion object {
        fun tfBin(project: Project) = File(project.kotless.config.binDirectory, "terraform")
    }

    @get:Input
    val version: String
        get() = project.kotless.config.terraform.version

    @get:OutputFile
    val binFile: File
        get() = tfBin(project)

    @TaskAction
    fun act() {
        logger.lifecycle("Downloading terraform version $version for OS $os")

        Downloads.download(URL("https://releases.hashicorp.com/terraform/$version/terraform_${version}_$os.zip"), binFile.parentFile, Archive.ZIP)

        if (Os.isFamily(Os.FAMILY_MAC) || Os.isFamily(Os.FAMILY_UNIX)) {
            CommandLine.execute("chmod", listOf("+x", binFile.absolutePath), binFile.parentFile, redirectStdout = false)
        }

        logger.lifecycle("Terraform version $version for OS $os successfully downloaded")
    }
}
