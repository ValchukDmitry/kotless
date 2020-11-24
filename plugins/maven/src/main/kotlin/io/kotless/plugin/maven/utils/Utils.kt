package io.kotless.plugin.maven.utils
import java.io.File

internal fun File.clearDirectory() {
    require(exists().not() || isDirectory) { "Only directory can be cleared with `clearDirectory` call" }
    deleteRecursively()
    mkdirs()
}
