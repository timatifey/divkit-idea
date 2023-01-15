package dev.timatifey.divkit.idea.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.selectedValueIs
import dev.timatifey.divkit.idea.actions.PreviewAppAssembleAction
import dev.timatifey.divkit.idea.assembler.*
import javax.swing.JComboBox

private typealias Dependencies = Pair<Dependency.Remote, Dependency.Local>
private typealias ArtifactId = String

class PreviewAppSettingsConfigurable : BoundConfigurable("Preview Apk File") {
    private val settings: PreviewAppSettings.State
        get() = PreviewAppSettings.state

    private lateinit var dependencies: MutableMap<ArtifactId, Dependencies>

    private fun ArtifactId.toSettingsDependency() = settings.divKitDependencies[this]!!
    private fun ArtifactId.toInternalDependency() = dependencies[this]!!

    override fun createPanel(): DialogPanel = panel {
        dependencies = requiredDivKitDependencies.associateWith { artifactId ->
            when (val dependency = artifactId.toSettingsDependency()) {
                is Dependency.Local -> (Dependency.Remote(BASE_GROUP_ID, artifactId, BASE_VERSION) to dependency)
                is Dependency.Remote -> (dependency to Dependency.Local(""))
            }
        }.toMutableMap()

        row("Preview APK: ") {
            comboBox(
                    items = availableApkFiles.toTypedArray(),
                    renderer = SimpleListCellRenderer.create<PreviewApkFile>("") { it.displayName }
            ).bindItem(settings::selectedPreviewApk) {
                settings.selectedPreviewApk = it
            }
        }
        rowsRange {
            dependencies.keys.forEach { artifactId ->
                lateinit var comboBox: Cell<JComboBox<DivKitDependency>>
                var settingsDependency = when (settings.divKitDependencies[artifactId]!!) {
                    is Dependency.Remote -> DivKitDependency.Remote
                    is Dependency.Local -> DivKitDependency.Local
                }
                group(artifactId, indent = true) {
                    row {
                        comboBox = divKitDependencyTypeComboBox().bind(
                                { component -> component.selectedItem as DivKitDependency },
                                { component, value -> component.setSelectedItem(value) },
                                PropertyBinding(
                                        get = { settingsDependency },
                                        set = { dependencyType ->
                                            settings.divKitDependencies[artifactId] = when (dependencyType) {
                                                is DivKitDependency.Remote -> artifactId.toInternalDependency().first
                                                is DivKitDependency.Local -> artifactId.toInternalDependency().second
                                            }
                                            settingsDependency = dependencyType
                                        }
                                )
                        )
                    }
                    val internalDependency = artifactId.toInternalDependency()
                    localDependencyFields(artifactId, internalDependency)
                            .visibleIf(comboBox.component.selectedValueIs(DivKitDependency.Local))
                    remoteDependencyFields(artifactId, internalDependency)
                            .visibleIf(comboBox.component.selectedValueIs(DivKitDependency.Remote))
                }
            }
            row {
                button(text = "Assemble preview app APK", action = PreviewAppAssembleAction())
            }
        }
    }

    private fun Row.divKitDependencyTypeComboBox(): Cell<JComboBox<DivKitDependency>> = comboBox(
            items = arrayOf(DivKitDependency.Remote, DivKitDependency.Local),
            renderer = SimpleListCellRenderer.create<DivKitDependency>("") {
                when (it) {
                    is DivKitDependency.Local -> "Local"
                    is DivKitDependency.Remote -> "Remote"
                }
            }
    )

    private fun Panel.remoteDependencyFields(artifactId: ArtifactId, pair: Dependencies) = rowsRange {
        val remote = pair.first
        row("groupId") {
            textField().bindText(remote::groupId) {
                val newRemote = remote.copy(groupId = it)
                dependencies[artifactId] = pair.copy(first = newRemote)
                settings.divKitDependencies[artifactId] = newRemote
            }
        }

        row("artifactId") {
            textField().bindText(remote::artifactId) {
                val newRemote = remote.copy(artifactId = it)
                dependencies[artifactId] = pair.copy(first = newRemote)
                settings.divKitDependencies[artifactId] = newRemote
            }
        }
        row("version") {
            textField().bindText(remote::version) {
                val newRemote = remote.copy(version = it)
                dependencies[artifactId] = pair.copy(first = newRemote)
                settings.divKitDependencies[artifactId] = newRemote
            }
        }
    }

    private fun Panel.localDependencyFields(artifactId: ArtifactId, pair: Dependencies) = rowsRange {
        val local = pair.second
        row("artifactPath") {
            textFieldWithBrowseButton().bindText(local::artifactPath) {
                val newLocal = local.copy(artifactPath = it)
                dependencies[artifactId] = pair.copy(second = newLocal)
                settings.divKitDependencies[artifactId] = newLocal
            }
        }
    }
}
