package dev.timatifey.divkit.idea.adb

interface ConnectedDeviceWrapper {
    val serialNumber: String

    fun startPreviewActivity(port: Int, receiver: ShellOutReceiver)
    fun createForward(localPort: Int, remotePort: Int)
    fun removeForward(localPort: Int, remotePort: Int)
    fun install(packageFilePath: String, reinstall: Boolean)
}
