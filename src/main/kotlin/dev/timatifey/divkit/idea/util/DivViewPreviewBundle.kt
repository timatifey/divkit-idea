@file:JvmName(name = "DivViewPreviewBundle")

package dev.timatifey.divkit.idea.util

import com.intellij.AbstractBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*

const val BUNDLE: String = "dev.timatifey.divkit.idea.util.DivViewPreviewBundle"


fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String =
    AbstractBundle.message(getBundle(), key, *params)

private var ourBundle: Reference<ResourceBundle>? = null

private fun getBundle(): ResourceBundle {
    var bundle = SoftReference.dereference(ourBundle)

    if (bundle == null) {
        bundle = ResourceBundle.getBundle(BUNDLE)!!
        ourBundle = SoftReference(bundle)
    }

    return bundle
}