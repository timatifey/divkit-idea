package dev.timatifey.divkit.idea.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class DivKitIcons {
    companion object {
        private fun load(path: String): Icon {
            return IconLoader.getIcon(path, DivKitIcons::class.java)
        }

        val RUN_CONFIGURATION: Icon = load("pluginIcon.svg")
    }

}