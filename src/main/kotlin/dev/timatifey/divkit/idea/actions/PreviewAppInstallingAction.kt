package dev.timatifey.divkit.idea.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import dev.timatifey.divkit.idea.settings.PreviewAppSettings
import dev.timatifey.divkit.idea.settings.availableApkFiles
import dev.timatifey.divkit.idea.common.context

class PreviewAppInstallingAction: AnAction(
        "Install Preview App",
        "Installing preview app on device",
        AllIcons.Toolwindows.ToolWindowRun
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val context = project.context()
        val pluginLogger = context.logger
        var apkFile = PreviewAppSettings.state.selectedPreviewApk
        if (apkFile == null) {
            PreviewAppSettings.state.selectedPreviewApk = availableApkFiles.firstOrNull()
            apkFile = PreviewAppSettings.state.selectedPreviewApk ?: return
        }
        val devices = context.adb.deviceList()
        pluginLogger.i("Install action into $devices")
        object : Task.Backgroundable(project, "Title", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Installing preview app"
                devices.forEach { device ->
                    device.install(apkFile.pathString, reinstall = true)
                }
            }
        }.queue()
    }
}
