package dev.timatifey.divkit.idea.adb

import com.intellij.openapi.project.Project
import dev.timatifey.divkit.idea.common.AppLogger
import org.jetbrains.android.sdk.AndroidSdkUtils

class AdbWrapperImpl(
        project: Project,
        private val logger: AppLogger
) : AdbWrapper {
    private val androidBridge = AndroidSdkUtils.getDebugBridge(project)

    override fun addDeviceChangedListener(listener: DeviceChangedListener) {
        // not used.
    }

    override fun allowedToConnectAndStop(): Boolean = false

    override fun connect() = Unit

    override fun connect(remoterAddress: String) = Unit

    override fun deviceList(): List<ConnectedDeviceWrapper> {
        val devices = androidBridge?.devices?.asList() ?: emptyList()

        val wrappers = mutableListOf<ConnectedDeviceWrapper>()
        devices.forEach {
            wrappers.add(AdbConnectedDeviceWrapper(it))
        }
        return wrappers
    }

    override fun hasInitialDeviceList(): Boolean = androidBridge?.hasInitialDeviceList() ?: false

    override fun isConnected(): Boolean {
        if (androidBridge == null) {
            return false
        }

        return androidBridge.isConnected && androidBridge.hasInitialDeviceList()
    }

    override fun removeDeviceChangedListener(listener: DeviceChangedListener) {
    }

    override fun stop() = Unit
}