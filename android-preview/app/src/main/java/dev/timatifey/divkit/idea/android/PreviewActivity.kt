package dev.timatifey.divkit.idea.android

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class PreviewActivity : ComponentActivity() {
    private val TAG = "PreviewActivity"
    private var divView: Div2View? = null
    private var socketThread: SocketThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) {
            Log.d(TAG, "Application is not debuggable. Compose Preview not allowed.")
            finish()
            return
        }
        val port = intent?.getStringExtra("port")?.toInt()
        if (port == null) {
            Log.d(TAG, "port is null")
            finish()
            return
        }
        divView = createDiv2View()
        socketThread = SocketThread(port).also { it.start() }
        setContentView(divView)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketThread?.isEnd = true
        socketThread = null
    }

    inner class SocketThread(private val port: Int): Thread() {
        var isEnd = false
        override fun run() {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Opened server socket")
                while (!isEnd) {
                    val socket = serverSocket.accept()
                    Log.d(TAG, "Accepted socket")
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    val builder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        builder.appendLine(line)
                    }
                    val data = builder.toString()
                    Log.d(TAG, "Set ${data.take(20)}")
                    val divData = buildDivData(JSONObject(data))
                    runOnUiThread {
                        divView?.setData(divData, DivDataTag(divData.logId))
                    }
                }
            } finally {
                serverSocket?.close()
                Log.d(TAG, "Closed server socket")
            }
        }
    }
    private fun createDiv2View(): Div2View {
        val divContext = Div2Context(baseContext = this, configuration = createDivConfiguration())
        val divView = Div2View(divContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return divView
    }

    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(DemoDivImageLoader(this))
            .actionHandler(DemoDivActionHandler())
            .supportHyphenation(true)
            .visualErrorsEnabled(true)
            .build()
    }

    private fun buildDivData(divJson: JSONObject): DivData {
        val templatesJson = divJson.optJSONObject("templates")
        val cardJson = divJson.getJSONObject("card")
        val parsingEnvironment = DivParsingEnvironment(ParsingErrorLogger.ASSERT).apply {
            if (templatesJson != null) parseTemplates(templatesJson)
        }
        return DivData(parsingEnvironment, cardJson)
    }
    class DemoDivActionHandler: DivActionHandler()

}
