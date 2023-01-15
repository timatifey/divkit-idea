package dev.timatifey.divkit.idea.util

import com.intellij.openapi.actionSystem.DataKey
import org.json.JSONObject

private const val PREFIX = "DivViewPreview"
internal val RAW_DIV_DATA = DataKey.create<JSONObject>(
    "$PREFIX.RawDivData")