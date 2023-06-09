package dev.timatifey.divkit.idea.dsl

import com.android.tools.idea.kotlin.getClassName
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class DivKitPreviewRunConfigurationProducer : LazyRunConfigurationProducer<DivKitPreviewRunConfiguration>() {
    override fun getConfigurationFactory() =
            runConfigurationType<DivKitPreviewRunConfigurationType>().configurationFactories[0]

    override fun setupConfigurationFromContext(configuration: DivKitPreviewRunConfiguration,
                                               context: ConfigurationContext,
                                               sourceElement: Ref<PsiElement>): Boolean {

        context.containingDivKitPreviewFunction()?.let {
            val bodyExpressionText = it.bodyExpression?.node?.text
            configuration.name = it.name!!
            configuration.divKitPreviewMethodFqn = it.divKitPreviewFunctionFqn()
            configuration.setupFilePath(pathFromContext(context) ?: return false)
            configuration.setGeneratedName()
            configuration.setModule(context.module)
            return true
        }
        return false
    }

    fun pathFromContext(context: ConfigurationContext?): String? {
        val location = context?.location ?: return null
        return pathFromPsiElement(location.psiElement)
    }

    override fun isConfigurationFromContext(configuration: DivKitPreviewRunConfiguration, context: ConfigurationContext): Boolean {
        context.containingDivKitPreviewFunction()?.let {
            return configuration.name == it.name && configuration.divKitPreviewMethodFqn == it.divKitPreviewFunctionFqn()
        }
        return false
    }

    companion object {
        fun pathFromPsiElement(element: PsiElement): String? {
            val file = element.getParentOfType<KtFile>(false) ?: return null
            val script = file.script ?: return null
            return script.containingKtFile.virtualFile.canonicalPath
        }
    }
}

private fun KtNamedFunction.divKitPreviewFunctionFqn() = "${getClassName()}.${name}"

private fun ConfigurationContext.containingDivKitPreviewFunction() =
        psiLocation?.let { location -> location.getNonStrictParentOfType<KtNamedFunction>()?.takeIf { it.isValidDivKitPreview() } }
