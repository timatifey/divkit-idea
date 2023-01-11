package dev.timatifey.divkit.idea.preview.runconfiguration

import com.android.tools.adtui.TabularLayout
import com.android.tools.idea.run.editor.AndroidDebuggerPanel
import com.intellij.application.options.ModulesComboBox
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import dev.timatifey.divkit.idea.preview.message
import org.jetbrains.android.facet.AndroidFacet
import java.awt.BorderLayout
import java.nio.file.Path
import javax.swing.JPanel

/**
 * Represents the UI for editing and creating instances of [DivViewPreviewRunConfiguration] in the run configurations edit panel.
 */
class DivViewPreviewSettingsEditor(private val project: Project, private val config: DivViewPreviewRunConfiguration) :
    SettingsEditor<DivViewPreviewRunConfiguration>() {
    private val panel: JPanel
    private val debuggerTab: AndroidDebuggerPanel?
    private val modulesComboBox = ModulesComboBox()
    private val jsonFilePathField = JBTextField().apply {
        emptyText.text = message("run.configuration.json.path.empty.text")
    }

    init {
        Disposer.register(project, this)
        panel = JPanel(TabularLayout("*", "Fit,*"))
        val tabbedPane = JBTabbedPane()
        tabbedPane.add(message("run.configuration.general.tab"), createGeneralTab())
        debuggerTab = createDebuggerTab()
        debuggerTab?.component?.let {
            tabbedPane.add(message("run.configuration.debugger.tab"), it)
        }

        panel.add(tabbedPane, TabularLayout.Constraint(0, 0))
    }

    private fun createGeneralTab(): JPanel {
        val tab = JPanel(TabularLayout("Fit,*", "Fit,Fit"))
        modulesComboBox.allowEmptySelection(message("run.configuration.no.module.selected"))
        tab.add(
            LabeledComponent.create(modulesComboBox, message("run.configuration.module.label"), BorderLayout.WEST),
            TabularLayout.Constraint(0, 0)
        )
        tab.add(
            LabeledComponent.create(jsonFilePathField, message("run.configuration.json.path.label"), BorderLayout.WEST),
            TabularLayout.Constraint(1, 0, 2)
        )
        return tab
    }

    private fun createDebuggerTab(): AndroidDebuggerPanel? {
        val debuggerContext = config.androidDebuggerContext
        modulesComboBox.addActionListener { debuggerContext.setDebuggeeModuleProvider { modulesComboBox.selectedModule } }
        return if (debuggerContext.androidDebuggers.size > 1) AndroidDebuggerPanel(config, debuggerContext) else null
    }

    private fun resetComboBoxModules() {
        modulesComboBox.setModules(ModuleManager.getInstance(project).modules.filter {
            AndroidFacet.getInstance(it)?.let { facet ->
                return@filter !facet.configuration.isLibraryProject
            }
            return@filter false
        })
    }

    override fun resetEditorFrom(runConfiguration: DivViewPreviewRunConfiguration) {
        resetComboBoxModules()
        runConfiguration.modules.takeUnless { it.isEmpty() }?.let {
            modulesComboBox.selectedModule = it[0]
        }
        runConfiguration.jsonFilePath?.let { jsonFilePathField.text = it.toString() }
        debuggerTab?.resetFrom(runConfiguration.androidDebuggerContext)
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(runConfiguration: DivViewPreviewRunConfiguration) {
        debuggerTab?.applyTo(runConfiguration.androidDebuggerContext)
        runConfiguration.jsonFilePath = Path.of(jsonFilePathField.text)
        runConfiguration.setModule(modulesComboBox.selectedModule)
    }

    override fun createEditor() = panel
}
