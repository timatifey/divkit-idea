package dev.timatifey.divkit.idea.dsl

import com.android.tools.idea.compose.preview.util.toSmartPsiPointer
import com.android.tools.idea.kotlin.fqNameMatches
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.uast.*
import org.jetbrains.uast.kotlin.KotlinUClassLiteralExpression
import org.jetbrains.uast.visitor.UastVisitor

private fun UAnnotation.findAttributeIntValue(name: String) =
        findAttributeValue(name)?.evaluate() as? Int

private fun UAnnotation.findAttributeFloatValue(name: String) =
        findAttributeValue(name)?.evaluate() as? Float

private fun UAnnotation.findClassNameValue(name: String) =
        (findAttributeValue(name) as? KotlinUClassLiteralExpression)?.type?.canonicalText

object AnnotationFilePreviewElementFinder {
    fun hasPreviewMethods(project: Project, vFile: VirtualFile): Boolean = ReadAction.compute<Boolean, Throwable> {
        val psiFile = PsiManager.getInstance(project).findFile(vFile)
        PsiTreeUtil.findChildrenOfType(psiFile, KtImportDirective::class.java)
                .any { PREVIEW_ANNOTATION_FQN == it.importedFqName?.asString() } ||
                PsiTreeUtil.findChildrenOfType(psiFile, KtAnnotationEntry::class.java)
                        .any { it.fqNameMatches(PREVIEW_ANNOTATION_FQN) }
    }

    /**
     * Returns all the `@Composable` functions in the [uFile] that are also tagged with `@Preview`.
     */
    fun findPreviewMethods(uFile: UFile): Sequence<PreviewElement> = ReadAction.compute<Sequence<PreviewElement>, Throwable> {
        if (DumbService.isDumb(uFile.sourcePsi.project)) {
            Logger.getInstance(AnnotationFilePreviewElementFinder::class.java)
                    .debug("findPreviewMethods called in dumb mode. No annotations will be found")
            return@compute sequenceOf()
        }

        val previewMethodsFqName = mutableSetOf<String>()
        val previewElements = mutableListOf<PreviewElement>()
        uFile.accept(object : UastVisitor {
            // Return false so we explore all the elements in the file (in case there are multiple @Preview elements)
            override fun visitElement(node: UElement): Boolean = false

            /**
             * Called for every `@DivKitPreview` annotation.
             */
            private fun visitPreviewAnnotation(previewAnnotation: UAnnotation, annotatedMethod: UMethod) {
                val composableMethod = annotatedMethod.name

                // If the same composable functions is found multiple times, only keep the first one. This usually will happen during
                // copy & paste and both the compiler and Studio will flag it as an error.
                if (previewMethodsFqName.add(composableMethod)) {
                    val basePreviewElement = PreviewElement(
                            composableMethodFqn = composableMethod,
                            previewElementDefinitionPsi = previewAnnotation.toSmartPsiPointer(),
                            previewBodyPsi = annotatedMethod.uastBody.toSmartPsiPointer())
                    previewElements.add(basePreviewElement)
                }
            }

            override fun visitAnnotation(node: UAnnotation): Boolean {
                if (PREVIEW_ANNOTATION_FQN == node.qualifiedName) {
                    node.getContainingUMethod()?.let { uMethod ->
                        visitPreviewAnnotation(node, uMethod)
                    }
                }

                return super.visitAnnotation(node)
            }
        })
        return@compute previewElements.asSequence()
    }
}
