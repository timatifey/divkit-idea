package dev.timatifey.divkit.idea.actions

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.notificationGroup
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.task.ProjectTaskManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.EnvironmentUtil
import com.intellij.util.PathUtil
import dev.timatifey.divkit.idea.assembler.Dependency
import dev.timatifey.divkit.idea.assembler.PreviewAppProjectGenerator
import dev.timatifey.divkit.idea.assembler.defaultRemoteDivKitDependencies
import dev.timatifey.divkit.idea.common.apkDirectory
import dev.timatifey.divkit.idea.settings.PreviewAppSettings
import dev.timatifey.divkit.idea.settings.availableApkFiles
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

internal class PreviewAppAssembleAction : AnAction(
    "Preview App Generation Action",
    "Generate android preview app apk",
    AllIcons.Toolwindows.ToolWindowBuild
) {

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dependencies = PreviewAppSettings.state.divKitDependencies.values.toList()
        ProjectTaskManager.getInstance(project).apply {
            run(createModulesBuildTask(
                    /* modules = */ ModuleManager.getInstance(project).modules,
                    /* isIncrementalBuild = */ true,
                    /* includeDependentModules = */ true,
                    /* includeRuntimeDependencies = */ true
            )).onSuccess {
                val previewAppProjectPath = PreviewAppProjectGenerator.createProject(dependencies)
                if (previewAppProjectPath == null) {
                    notificationGroup.createNotification(
                            title = "Assembling failed",
                            content = "Could not to find app preview template",
                            type = NotificationType.ERROR
                    ).notify(project)
                    return@onSuccess
                }
                notificationGroup.createNotification(
                        title = "Assembling started",
                        content = "Started assembling project at $previewAppProjectPath",
                        type = NotificationType.INFORMATION
                ).notify(project)
                val deployDir = apkDirectory
                runAssembleCommand(project, deployDir, previewAppProjectPath)
            }
        }
    }

    private fun runAssembleCommand(
        project: Project,
        deployDirPath: String,
        previewAppProjectPath: String,
    ) {
        object : Task.Backgroundable(project, "Title", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Assembling preview app"
                val scriptHandler = OSProcessHandler(buildAssembleCommandLine(previewAppProjectPath).apply {
                    isRedirectErrorStream = true
                })

                scriptHandler.addProcessListener(buildAssemblingProcessListener(
                        project = project,
                        indicator = indicator,
                        output = Collections.synchronizedList(ArrayList<@NlsSafe String>()),
                        deployDirPath = deployDirPath,
                        previewAppProjectPath = previewAppProjectPath
                ))
                scriptHandler.startNotify()
                while (!scriptHandler.isProcessTerminated) {
                    scriptHandler.waitFor(300)
                    indicator.checkCanceled()
                }
            }
        }.queue()
    }

    private fun buildAssemblingProcessListener(
            project: Project,
            indicator: ProgressIndicator,
            output: MutableList<String>,
            deployDirPath: String,
            previewAppProjectPath: String,
    ) = object : ProcessListener {
        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
            output.add(event.text)
            if (outputType == ProcessOutputTypes.STDOUT) {
                indicator.text2 = event.text
            }
        }

        override fun startNotified(event: ProcessEvent) {}

        override fun processTerminated(event: ProcessEvent) {
            if (indicator.isCanceled) {
                return
            }

            val outputContent = output.joinToString("")

            if (event.exitCode != 0) {
                notifyAssemblingFailed(project, event.exitCode, outputContent, deployDirPath)
                return
            }

            val sourceApkFilePath = "$previewAppProjectPath/app/build/outputs/apk/debug/app-debug.apk"
            val suffix = when (val dep = PreviewAppSettings.state.divKitDependencies.values.first()) {
                is Dependency.Remote -> dep.version
                is Dependency.Local -> dep.artifactPath
            }
            val apkFileName = "divkit-preview-app-$suffix.apk"
            val destinationApkFilePath = "$deployDirPath/$apkFileName"

            Files.move(Path.of(sourceApkFilePath), Path.of(destinationApkFilePath))
            if (PreviewAppSettings.state.selectedPreviewApk == null) {
                PreviewAppSettings.state.selectedPreviewApk = availableApkFiles.firstOrNull()
            }
            notifyAssemblingSuccess(project, outputContent, destinationApkFilePath)
        }
    }

    private fun notifyAssemblingFailed(
            project: Project,
            exitCode: Int,
            outputContent: String,
            deployDirPath: String,
    ) {
        notificationGroup.createNotification(
                title = "Assembling failed",
                content = "Assembling of preview app failed with code = $exitCode",
                type = NotificationType.ERROR
        )
                .addAction(NotificationAction.createSimple("Open output.txt") {
                    FileEditorManager.getInstance(project)
                            .openFile(LightVirtualFile("output.txt", outputContent), true)
                })
                .addAction(NotificationAction.createSimple("Open debug.log") {
                    val logFile = LocalFileSystem.getInstance().refreshAndFindFileByPath("$deployDirPath/log/debug.log")
                            ?: return@createSimple
                    logFile.refresh(true, false)
                    FileEditorManager.getInstance(project).openFile(logFile, true)
                })
                .notify(project)
    }

    private fun notifyAssemblingSuccess(project: Project, outputContent: String, apkFilePath: String) {
        notificationGroup.createNotification(
                title = "Assembling success",
                content = "Assembling of preview app success. Project apk: $apkFilePath",
                type = NotificationType.INFORMATION
        )
                .addAction(NotificationAction.createSimple("Open output.txt") {
                    FileEditorManager.getInstance(project)
                            .openFile(LightVirtualFile("output.txt", outputContent), true)
                })
                .notify(project)
    }

    private fun buildAssembleCommandLine(previewAppProjectPath: String): GeneralCommandLine {
        val command = arrayOf(
            "cd $previewAppProjectPath",
            "ls -l",
            "chmod +x gradlew",
            "./gradlew init",
            "./gradlew assemble"
        )
        val commandLine = PtyCommandLine().apply {
            withConsoleMode(false)
            withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            setWorkDirectory(previewAppProjectPath)
            withExePath(EnvironmentUtil.getValue("SHELL") ?: "/bin/sh")
            withParameters("-c", command.joinToString(" && "))
        }
        return commandLine
    }
}
