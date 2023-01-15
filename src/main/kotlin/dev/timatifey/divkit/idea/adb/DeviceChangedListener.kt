package dev.timatifey.divkit.idea.adb

const val CHANGE_CLIENT_LIST = 0x0002
const val CHANGE_STATE = 0x0001
const val CHANGE_BUILD_INFO = 0x0004

interface DeviceChangedListener {
    /**
     * Sent when the device is connected to the [AndroidDebugBridge].
     *
     *
     * This is sent from a non UI thread.
     * @param device the new device.
     */
    fun deviceConnected(device: ConnectedDeviceWrapper)

    /**
     * Sent when the a device is connected to the [AndroidDebugBridge].
     *
     *
     * This is sent from a non UI thread.
     * @param device the new device.
     */
    fun deviceDisconnected(device: ConnectedDeviceWrapper)

    /**
     * Sent when a device data changed, or when clients are started/terminated on the device.
     *
     *
     * This is sent from a non UI thread.
     * @param device the device that was updated.
     * @param changeMask the mask describing what changed. It can contain any of the following
     * values: [CHANGE_BUILD_INFO], [CHANGE_STATE],
     * [CHANGE_CLIENT_LIST]
     */
    fun deviceChanged(device: ConnectedDeviceWrapper, changeMask: Int)

    /**
     * Is called when called deviceChanged with mask [CHANGE_CLIENT_LIST]
     */
    fun deviceClientsListChanged(device: ConnectedDeviceWrapper)
}