package dev.timatifey.divkit.idea.dsl

import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.util.NotNullLazyValue
import icons.StudioIcons
import com.intellij.openapi.project.Project

class DivKitPreviewRunConfigurationType : SimpleConfigurationType(
        id = "DivKitPreviewRunConfiguration",
        name = "DivKit Preview",
        description = "DivKit Preview Run Configuration Type",
        icon = NotNullLazyValue.createValue { StudioIcons.Compose.Toolbar.RUN_CONFIGURATION }
) {
    override fun createTemplateConfiguration(project: Project) = DivKitPreviewRunConfiguration(project, this, null)
}
