package dev.timatifey.divkit.idea.adb

/**
 * Classes which implement this interface provide methods that deal with out from a remote shell
 * command on a device/emulator.
 */
interface ShellOutReceiver {
    /**
     * Called every time some new data is available.
     * @param data The new data.
     * @param offset The offset at which the new data starts.
     * @param length The length of the new data.
     */
    fun addOutput(data: ByteArray, offset: Int, length: Int)

    /**
     * Called at the end of the process execution (unless the process was
     * canceled). This allows the receiver to terminate and flush whatever
     * data was not yet processed.
     */
    fun flush()

    /**
     * Cancel method to stop the execution of the remote shell command.
     * @return true to cancel the execution of the command.
     */
    fun isCancelled(): Boolean
}
