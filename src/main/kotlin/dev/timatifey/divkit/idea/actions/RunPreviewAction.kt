package dev.timatifey.divkit.idea.actions

import com.android.tools.idea.util.toIoFile
import com.google.common.io.ByteStreams
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import dev.timatifey.divkit.idea.adb.ConnectedDeviceWrapper
import dev.timatifey.divkit.idea.adb.ShellOutReceiver
import dev.timatifey.divkit.idea.common.context
import icons.StudioIcons.Compose.Toolbar.RUN_ON_DEVICE
import java.io.File
import java.lang.Thread.sleep
import java.net.Socket


/**
 * Action to deploy a json file to the device.
 */
internal class RunPreviewAction : AnAction(null, null, RUN_ON_DEVICE) {

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE)
        if (file.extension != "json") return
        val project = e.project ?: return
        val deviceList = project.context().adb.deviceList()
        runPreview(project, deviceList, file.toIoFile())
    }

    private fun runPreview(project: Project, deviceList: List<ConnectedDeviceWrapper>, jsonPreviewFile: File) {
        deviceList.forEach { device ->
            val shellOutput = object : ShellOutReceiver {
                override fun addOutput(data: ByteArray, offset: Int, length: Int) {
                    val output = data.toString(Charsets.UTF_8)
                    println(output)
                }

                override fun flush() {}
                override fun isCancelled(): Boolean {
                    return false
                }
            }
            device.startPreviewActivity(REMOTE_PORT, shellOutput)
            object : Task.Backgroundable(project, "Deploying preview", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.text = "Deploying preview"
                    sleep(400)
                    device.createForward(LOCAL_PORT, REMOTE_PORT)
                    sendJsonPreview(jsonPreviewFile)
                    device.removeForward(LOCAL_PORT, REMOTE_PORT)
                    indicator.text = "Success deploying preview"
                }
            }.queue()
        }
    }

    internal companion object {
        const val LOCAL_PORT = 8000
        const val REMOTE_PORT = 8000

        private fun sendJsonPreview(jsonPreviewFile: File) {
            val socket = Socket("127.0.0.1", LOCAL_PORT)
            val outputStream = socket.getOutputStream()
            val inputStream = jsonPreviewFile.inputStream()
            ByteStreams.copy(inputStream, outputStream)
            outputStream.flush()
            inputStream.close()
            outputStream.close()
            socket.close()
        }
    }
}
