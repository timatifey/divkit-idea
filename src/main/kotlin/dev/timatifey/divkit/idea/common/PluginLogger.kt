package dev.timatifey.divkit.idea.common

import com.intellij.openapi.diagnostic.Logger

class PluginLogger : AppLogger {
    private val log: Logger = Logger.getInstance("DIVKIT")
    override fun d(msg: String) {
        println(msg)
        log.debug(msg)
    }

    override fun e(msg: String) {
        println(msg)
        log.error(msg)
    }

    override fun e(msg: String, t: Throwable) {
        println(msg)
        log.error(msg, t)
    }

    override fun w(msg: String) {
        println(msg)
        log.warn(msg)
    }

    override fun i(msg: String) {
        println(msg)
        log.info(msg)
    }
}