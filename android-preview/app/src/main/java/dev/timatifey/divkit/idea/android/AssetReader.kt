package dev.timatifey.divkit.idea.android

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetReader(private val context: Context) {

    fun read(filename: String): JSONObject {
        val inputStream = context.assets.open(filename)
        val buffer = CharArray(BUFFER_SIZE)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder(inputStream.available())
        var read: Int
        while (reader.read(buffer).also { read = it } != -1) {
            builder.append(buffer, 0, read)
        }
        val data = builder.toString()
        return JSONObject(data)
    }

    companion object {
        private const val BUFFER_SIZE = 2048
    }
}
