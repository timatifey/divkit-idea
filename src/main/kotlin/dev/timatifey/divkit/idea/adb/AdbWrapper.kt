package dev.timatifey.divkit.idea.adb

interface AdbWrapper {
    fun isConnected(): Boolean

    /**
     * Returns whether the bridge has acquired the initial list from adb after being created.
     * <p>Calling {@link #getDevices()} right after {@link #createBridge(String, boolean)} will
     * generally result in an empty list. This is due to the internal asynchronous communication
     * mechanism with <code>adb</code> that does not guarantee that the {@link IDevice} list has been
     * built before the call to {@link #getDevices()}.
     * <p>The recommended way to get the list of {@link IDevice} objects is to create a
     * {@link IDeviceChangeListener} object.
     */
    fun hasInitialDeviceList(): Boolean
    fun deviceList(): List<ConnectedDeviceWrapper>

    /**
     * Returns true when allowed manual adb connection and disconnection.
     */
    fun allowedToConnectAndStop(): Boolean
    fun connect()
    fun connect(remoterAddress: String)
    fun stop()

    fun addDeviceChangedListener(listener: DeviceChangedListener)
    fun removeDeviceChangedListener(listener: DeviceChangedListener)
}