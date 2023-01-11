package dev.timatifey.divkit.idea.preview.runconfiguration

import com.android.tools.idea.run.AndroidRunConfiguration
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import dev.timatifey.divkit.idea.icons.DivKitIcons
import java.nio.file.Path

class DivViewPreviewRunConfiguration(project: Project, factory: ConfigurationFactory) : AndroidRunConfiguration(project, factory) {
//    var rawJson: JSONObject? = JSONObject()
//        set(value) {
//            field = value
//            updateActivityExtraFlags()
//        }

    var jsonFilePath: Path? = null
        set(value) {
            field = value
            updateActivityExtraFlags()
        }

    init {
//        setLaunchActivity("dev.timatifey.divkit.idea.android.PreviewActivity", true)
        Messages.showMessageDialog(
            project,
            "Developer: TODO(Starts PreviewActivity).",
            "Start PreviewActivity",
            DivKitIcons.RUN_CONFIGURATION
        )
    }

    private fun updateActivityExtraFlags() {
        ACTIVITY_EXTRA_FLAGS = (jsonFilePath?.let { "--es json_file $it" } ?: "")
        Messages.showMessageDialog(
            project,
            "json_file: $jsonFilePath",
            "Update ActivityExtraFlags",
            DivKitIcons.RUN_CONFIGURATION
        )
    }

    override fun isProfilable() = false

    override fun getConfigurationEditor() = DivViewPreviewSettingsEditor(project, this)
}