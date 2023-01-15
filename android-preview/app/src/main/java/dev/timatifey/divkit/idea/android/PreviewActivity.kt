package dev.timatifey.divkit.idea.android

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div2.DivData

class PreviewActivity : ComponentActivity() {
    private val TAG = "PreviewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) {
            Log.d(TAG, "Application is not debuggable. Compose Preview not allowed.")
            finish()
            return
        }
        intent?.getStringExtra("json_file")?.let { createDiv2View(it) }
        val jsonFilePath = intent?.getStringExtra("json_file")
        if (jsonFilePath == null) {
            Log.d(TAG, "json_file is null")
            finish()
            return
        }
        val divView = createDiv2View(jsonFilePath)
        setContentView(divView)
    }

    private fun createDiv2View(jsonFile: String): FrameLayout {
        Log.d(TAG, "PreviewActivity has jsonFile $jsonFile")
        val assetReader = AssetReader(applicationContext)

        val divJson = assetReader.read(jsonFile)
        val templatesJson = divJson.optJSONObject("templates")
        val cardJson = divJson.getJSONObject("card")

        val divContext = Div2Context(baseContext = this, configuration = createDivConfiguration())
        val parsingEnvironment = DivParsingEnvironment(ParsingErrorLogger.ASSERT).apply {
            if (templatesJson != null) parseTemplates(templatesJson)
        }
        val divView = Div2View(divContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val divData = DivData(parsingEnvironment, cardJson)
        divView.setData(divData, DivDataTag(divData.logId))
        return divView
    }

    private fun createDivConfiguration(): DivConfiguration {
        return DivConfiguration.Builder(DemoDivImageLoader(this))
            .actionHandler(DemoDivActionHandler())
            .supportHyphenation(true)
            .visualErrorsEnabled(true)
            .build()
    }

    class DemoDivActionHandler: DivActionHandler() {
    }

}