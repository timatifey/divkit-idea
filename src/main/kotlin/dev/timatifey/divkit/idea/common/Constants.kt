package dev.timatifey.divkit.idea.common

import com.intellij.openapi.application.PathManager

const val PLUGIN_NAME = "divkit-idea"
const val APK_DIRECTORY = "$PLUGIN_NAME/apk"
const val PREVIEW_ACTIVITY = "dev.timatifey.divkit.idea.android/.PreviewActivity"

val apkDirectory: String
    get() = PathManager.getPluginsPath() + "/$APK_DIRECTORY"