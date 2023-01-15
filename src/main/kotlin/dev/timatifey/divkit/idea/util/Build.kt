package dev.timatifey.divkit.idea.util

import com.android.SdkConstants
import com.android.tools.idea.flags.StudioFlags
import com.android.tools.idea.gradle.project.ProjectStructure
import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker
import com.android.tools.idea.gradle.project.build.invoker.TestCompileType
import com.android.tools.idea.gradle.project.facet.gradle.GradleFacet
import com.android.tools.idea.gradle.project.model.AndroidModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import java.util.*

private fun createBuildTasks(module: Module): String? {
    val gradlePath = GradleFacet.getInstance(module)?.configuration?.GRADLE_PROJECT_PATH ?: return null
    val currentVariant = AndroidModuleModel.get(module)?.selectedVariant?.name?.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else
            it.toString()
    } ?: return null
    return "${gradlePath}${SdkConstants.GRADLE_PATH_SEPARATOR}compile${currentVariant}Kotlin"
}

private fun createBuildTasks(modules: Collection<Module>): Map<Module, List<String>> =
    modules
        .mapNotNull {
            Pair(it, listOf(createBuildTasks(it) ?: return@mapNotNull null))
        }
        .filter { it.second.isNotEmpty() }
        .toMap()

/**
 * Triggers the build of the given [modules] by calling the compile`Variant`Kotlin task
 */
private fun requestKotlinBuild(project: Project, modules: Set<Module>) {
    val moduleFinder = ProjectStructure.getInstance(project).moduleFinder

    createBuildTasks(modules).forEach {
        val path = moduleFinder.getRootProjectPath(it.key)
        GradleBuildInvoker.getInstance(project).executeTasks(path.toFile(), it.value, emptyList())
    }
}

/**
 * Triggers the build of the given [modules] by calling the compileSourcesDebug task
 */
private fun requestCompileJavaBuild(project: Project, modules: Set<Module>) =
    GradleBuildInvoker.getInstance(project).compileJava(modules.toTypedArray(), TestCompileType.NONE)

internal fun requestBuild(project: Project, module: Module) {
    if (project.isDisposed || module.isDisposed) {
        return
    }

    val modules = mutableSetOf(module)
    ModuleUtil.collectModulesDependsOn(module, modules)

    if (StudioFlags.COMPOSE_PREVIEW_ONLY_KOTLIN_BUILD.get()) {
        requestKotlinBuild(project, modules)
    } else {
        requestCompileJavaBuild(project, modules)
    }
}