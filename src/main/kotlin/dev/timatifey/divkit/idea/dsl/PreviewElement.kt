package dev.timatifey.divkit.idea.dsl

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

data class PreviewElement(
        val composableMethodFqn: String,
        val previewElementDefinitionPsi: SmartPsiElementPointer<PsiElement>?,
        val previewBodyPsi: SmartPsiElementPointer<PsiElement>?
)
