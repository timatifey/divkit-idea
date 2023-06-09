package dev.timatifey.divkit.idea.dsl

import com.android.tools.idea.kotlin.fqNameMatches
import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parentOfType
import dev.timatifey.divkit.idea.icons.DivKitIcons
import icons.StudioIcons
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.allConstructors
import org.jetbrains.kotlin.psi.psiUtil.containingClass

private fun KtClass.hasDefaultConstructor() = allConstructors.isEmpty().or(allConstructors.any { it.valueParameters.isEmpty() })

private fun KtNamedFunction.isValidPreviewLocation(): Boolean {
    if (isTopLevel) {
        return true
    }

    if (parentOfType<KtNamedFunction>() == null) {
        // This is not a nested method
        val containingClass = containingClass()
        if (containingClass != null) {
            // We allow functions that are not top level defined in top level classes that have a default (no parameter) constructor.
            if (containingClass.isTopLevel() && containingClass.hasDefaultConstructor()) {
                return true
            }
        }
    }
    return false
}

fun KtNamedFunction.isValidDivKitPreview() =
        isValidPreviewLocation() && annotationEntries.any { annotation -> annotation.fqNameMatches(PREVIEW_ANNOTATION_FQN) }

class DivKitPreviewRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element !is LeafPsiElement) return null
        if (element.node.elementType != KtTokens.IDENTIFIER) return null

        (element.parent as? KtNamedFunction)?.takeIf { it.isValidDivKitPreview() }?.let {
            return Info(StudioIcons.Compose.Toolbar.RUN_CONFIGURATION, ExecutorAction.getActions(0)) { _ -> "Run ${it.name!!}" }
        }
        return null
    }
}
