package dev.timatifey.divkit.idea.preview.actions

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import dev.timatifey.divkit.idea.preview.runconfiguration.DivViewPreviewRunConfiguration
import dev.timatifey.divkit.idea.preview.runconfiguration.DivViewPreviewRunConfigurationType
import icons.StudioIcons.Compose.Toolbar.RUN_ON_DEVICE
import java.nio.file.Path


/**
 * Action to deploy a json file to the device.
 */
internal class DeployToDeviceAction:
    AnAction(null, null, RUN_ON_DEVICE) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
        if (file.extension != "json") return
        val project = e.project ?: return
        runPreviewConfiguration(project, file.toNioPath())
    }

    private fun runPreviewConfiguration(project: Project, path: Path) {
        val factory = runConfigurationType<DivViewPreviewRunConfigurationType>().configurationFactories[0]
        val composePreviewRunConfiguration = DivViewPreviewRunConfiguration(project, factory).apply {
            jsonFilePath = path
        }

        val configurationAndSettings = RunManager.getInstance(project).findSettings(composePreviewRunConfiguration)
            ?: RunManager.getInstance(project).createConfiguration(composePreviewRunConfiguration, factory).apply {
                isTemporary = true
            }.also { configAndSettings ->
                RunManager.getInstance(project).addConfiguration(configAndSettings)
            }

        RunManager.getInstance(project).selectedConfiguration = configurationAndSettings
        ProgramRunnerUtil.executeConfiguration(configurationAndSettings, DefaultRunExecutor.getRunExecutorInstance())
    }

}