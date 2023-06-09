package dev.timatifey.divkit.idea.dsl

import com.intellij.codeInspection.reference.EntryPoint
import com.intellij.codeInspection.reference.RefElement
import com.intellij.configurationStore.deserializeInto
import com.intellij.configurationStore.serializeObjectInto
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jdom.Element

class PreviewEntryPoint : EntryPoint() {
    private var ADD_PREVIEW_TO_ENTRIES: Boolean = true

    override fun isEntryPoint(refElement: RefElement, psiElement: PsiElement): Boolean = isEntryPoint(psiElement)

    override fun isEntryPoint(psiElement: PsiElement): Boolean =
            psiElement is PsiMethod && psiElement.hasAnnotation(PREVIEW_ANNOTATION_FQN)

    override fun readExternal(element: Element) = element.deserializeInto(this)

    override fun writeExternal(element: Element) {
        serializeObjectInto(this, element)
    }

    override fun getDisplayName(): String = "DivKit Preview"

    override fun isSelected(): Boolean = ADD_PREVIEW_TO_ENTRIES

    override fun setSelected(selected: Boolean) {
        this.ADD_PREVIEW_TO_ENTRIES = selected
    }

}
