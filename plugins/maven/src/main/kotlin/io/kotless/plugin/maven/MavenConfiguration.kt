package io.kotless.plugin.maven

import org.apache.maven.plugins.annotations.Parameter

data class MavenConfiguration(
    @Parameter val group: String
)
