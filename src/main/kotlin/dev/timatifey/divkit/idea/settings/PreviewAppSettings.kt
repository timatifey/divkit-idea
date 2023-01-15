package dev.timatifey.divkit.idea.settings

import com.intellij.openapi.components.*
import dev.timatifey.divkit.idea.assembler.BASE_VERSION
import dev.timatifey.divkit.idea.assembler.Dependency
import dev.timatifey.divkit.idea.assembler.DivKitDependency
import dev.timatifey.divkit.idea.assembler.defaultRemoteDivKitDependencies
import dev.timatifey.divkit.idea.common.apkDirectory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.streams.toList

@State(name = "PreviewAppSettings", storages = [Storage("preview.xml", roamingType = RoamingType.DISABLED)])
internal class PreviewAppSettings : PersistentStateComponent<PreviewAppSettings.State> {
    companion object {
        val state: State
            get() = service<PreviewAppSettings>().state
    }

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        this.state = state
    }

    data class State(
            var divKitDependencies: MutableMap<String, Dependency> = defaultRemoteDivKitDependencies(BASE_VERSION),
            var selectedPreviewApk: PreviewApkFile? = availableApkFiles.firstOrNull(),
    )
}

data class PreviewApkFile(
        val pathString: String,
        val displayName: String,
)

val availableApkFiles: List<PreviewApkFile>
    get() {
        val apkDirectoryPath = apkDirectory
        val directory = File(apkDirectoryPath)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return Files.walk(Path.of(apkDirectoryPath))
                .filter { filePath -> Files.isRegularFile(filePath) }
                .filter { filePath -> filePath.extension == "apk" }
                .map { filePath ->
                    PreviewApkFile(
                            pathString = filePath.toString(),
                            displayName = filePath.name
                    )
                }
                .toList()
    }
