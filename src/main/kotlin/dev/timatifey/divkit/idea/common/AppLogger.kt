package dev.timatifey.divkit.idea.common

interface AppLogger {
    fun d(msg: String)
    fun e(msg: String)
    fun e(msg: String, t: Throwable)
    fun w(msg: String)
    fun i(msg: String)
}