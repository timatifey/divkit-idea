package dev.timatifey.divkit.idea.common

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import dev.timatifey.divkit.idea.adb.AdbWrapper
import dev.timatifey.divkit.idea.adb.AdbWrapperImpl

@Service(Service.Level.PROJECT)
class PluginContextService(private val project: Project) {
    val logger: AppLogger by lazy { PluginLogger() }
    val adb: AdbWrapper by lazy { AdbWrapperImpl(project, logger) }
}

fun Project.context(): PluginContextService = service<PluginContextService>()
