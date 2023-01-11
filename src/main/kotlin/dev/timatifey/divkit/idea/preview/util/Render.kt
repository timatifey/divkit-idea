package dev.timatifey.divkit.idea.preview.util

import com.android.tools.idea.common.surface.DesignSurface
import com.android.tools.idea.rendering.RenderResult
import com.android.tools.idea.uibuilder.scene.LayoutlibSceneManager


/**
 * Extension implementing some heuristics to detect Compose rendering errors. This allows to identify render
 * errors better.
 */
internal fun RenderResult?.isDivErrorResult(): Boolean {
    if (this == null) {
        return true
    }

    // DivView renders might fail with onLayout exceptions hiding actual errors. This will return an empty image
    // result. We can detect this by checking for a 1x1 image and the logger having errors.
    if (logger.hasErrors() && renderedImage.width == 1 && renderedImage.height == 1) {
        return true
    }

    return logger.brokenClasses.values
        .any {
            it is ReflectiveOperationException && it.stackTrace.any { ex -> DIV_VIEW_ADAPTER == ex.className }
        }
}

/**
 * Returns all the [LayoutlibSceneManager] belonging to the [DesignSurface].
 */
internal val DesignSurface.layoutlibSceneManagers: Sequence<LayoutlibSceneManager>
    get() = models.asSequence()
        .mapNotNull { getSceneManager(it) }
        .filterIsInstance<LayoutlibSceneManager>()