package dev.timatifey.divkit.idea.assembler

import com.intellij.openapi.util.io.FileUtil
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream


internal object PreviewAppProjectGenerator {

    private const val BUFFER_SIZE = 2048

    private val baseDependencies = listOf<Dependency>(
        Dependency.Remote(
            groupId = "androidx.core",
            artifactId = "core-ktx",
            version = "1.9.0",
        ),
        Dependency.Remote(
            groupId = "androidx.appcompat",
            artifactId = "appcompat",
            version = "1.5.1",
        ),
        Dependency.Remote(
            groupId = "com.google.android.material",
            artifactId = "material",
            version = "1.7.0",
        ),
        Dependency.Remote(
            groupId = "androidx.constraintlayout",
            artifactId = "constraintlayout",
            version = "2.1.4",
        ),
        Dependency.Remote(
            groupId = "com.squareup.picasso",
            artifactId = "picasso",
            version = "2.8",
        ),
        Dependency.Remote(
            groupId = "com.neovisionaries",
            artifactId = "nv-websocket-client",
            version = "2.14",
        ),
    )

    internal fun createProject(divKitDependencies: List<Dependency>): String? {
        val previewProject = this::class.java.getResourceAsStream("/app_preview_template.zip") ?: return null
        val tempPreviewProjectDirectory: Path = Files.createTempDirectory("app_preview_template_temp_dir")
        ZipInputStream(previewProject).use { zipInputStream ->
            extract(zipInputStream, tempPreviewProjectDirectory.toFile())
        }
        val absolutePath = tempPreviewProjectDirectory.toAbsolutePath().toString() + "/android-preview"
        appendDependenciesBlock("$absolutePath/app/build.gradle", divKitDependencies)
        return absolutePath
    }

    private fun makeDependenciesBlock(divKitDependencies: List<Dependency>): StringBuilder =
        StringBuilder().apply {
            appendLine("dependencies {")
            (baseDependencies + divKitDependencies).forEach { dependency ->
                appendLine(dependency.implementation().prependIndent())
            }
            appendLine("}")
        }

    private fun appendDependenciesBlock(buildGradleFilePath: String, divKitDependencies: List<Dependency>) {
        val buildGradleFile = File(buildGradleFilePath)
        FileUtil.appendToFile(buildGradleFile, makeDependenciesBlock(divKitDependencies).toString())
    }

    @Throws(IOException::class)
    private fun extract(zipInputStream: ZipInputStream, target: File) {
        zipInputStream.use { inputStream ->
            generateSequence {
                inputStream.nextEntry
            }.forEach { entry ->
                val file = File(target, entry.name)
                if (!file.toPath().normalize().startsWith(target.toPath())) {
                    throw IOException("Bad zip entry")
                }
                if (entry.isDirectory) {
                    file.mkdirs()
                    return@forEach
                }
                val buffer = ByteArray(BUFFER_SIZE)
                file.parentFile.mkdirs()
                BufferedOutputStream(FileOutputStream(file)).use { output ->
                    var count: Int
                    while (inputStream.read(buffer).also { count = it } != -1) {
                        output.write(buffer, 0, count)
                    }
                }
            }
        }
    }
}
