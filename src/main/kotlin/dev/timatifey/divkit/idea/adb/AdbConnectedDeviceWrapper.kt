package dev.timatifey.divkit.idea.adb

import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import dev.timatifey.divkit.idea.common.PREVIEW_ACTIVITY

class AdbConnectedDeviceWrapper(private val device: IDevice) : ConnectedDeviceWrapper {
    override val serialNumber: String
        get() = device.serialNumber

    override fun startPreviewActivity(port: Int, receiver: ShellOutReceiver) {
        val command = "am start -n $PREVIEW_ACTIVITY --es port $port"
        device.executeShellCommand(command, IShellOutReceiverAdapter(receiver))
    }

    override fun createForward(localPort: Int, remotePort: Int) {
        device.createForward(localPort, remotePort)
    }

    override fun removeForward(localPort: Int, remotePort: Int) {
        device.removeForward(localPort, remotePort)
    }

    override fun install(packageFilePath: String, reinstall: Boolean) {
        device.installPackage(packageFilePath, reinstall)
    }

    private class IShellOutReceiverAdapter(
            private val receiver: ShellOutReceiver
    ) : IShellOutputReceiver {
        override fun addOutput(data: ByteArray, offset: Int, length: Int) {
            receiver.addOutput(data, offset, length)
        }

        override fun flush() = receiver.flush()

        override fun isCancelled() = receiver.isCancelled()
    }
}
