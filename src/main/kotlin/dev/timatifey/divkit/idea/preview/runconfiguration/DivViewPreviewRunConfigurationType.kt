package dev.timatifey.divkit.idea.preview.runconfiguration

import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import dev.timatifey.divkit.idea.icons.DivKitIcons

/** A type for run configurations that launch DivView Previews to a device/emulator. */
class DivViewPreviewRunConfigurationType : SimpleConfigurationType(
    "DivViewPreviewRunConfiguration",
    "DivView Preview",
    "DivView Preview Run Configuration Type",
    NotNullLazyValue.createValue { DivKitIcons.RUN_CONFIGURATION }
) {
    override fun createTemplateConfiguration(project: Project) = DivViewPreviewRunConfiguration(project, this)
}