//package dev.timatifey.divkit.idea.dsl
//
//import com.intellij.execution.configurations.JavaCommandLineState
//import com.intellij.execution.process.CapturingProcessHandler
//import com.intellij.execution.target.TargetEnvironmentRequest
//import com.intellij.execution.target.TargetProgressIndicatorAdapter
//import com.intellij.execution.target.TargetedCommandLine
//import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
//import com.intellij.openapi.diagnostic.ControlFlowException
//import com.intellij.openapi.module.Module
//import com.intellij.openapi.progress.ProgressIndicator
//import com.intellij.openapi.progress.Task
//import com.intellij.openapi.project.DumbService
//import com.intellij.openapi.project.Project
//import com.intellij.openapi.util.Computable
//import com.intellij.openapi.util.NlsContexts
//import com.intellij.openapi.util.io.FileUtil
//import com.intellij.openapi.vfs.VirtualFile
//import com.intellij.psi.PsiFile
//import org.jetbrains.kotlin.idea.KotlinJvmBundle
//import org.jetbrains.kotlin.idea.core.KotlinCompilerIde
//import org.jetbrains.kotlin.idea.core.script.ScriptConfigurationManager
//import org.jetbrains.kotlin.idea.core.util.toPsiFile
//import org.jetbrains.kotlin.idea.scratch.LOG
//import org.jetbrains.kotlin.idea.scratch.ScratchExpression
//import org.jetbrains.kotlin.idea.scratch.compile.KtCompilingExecutor
//import org.jetbrains.kotlin.idea.scratch.compile.KtScratchSourceFileProcessor
//import org.jetbrains.kotlin.idea.scratch.printDebugMessage
//import org.jetbrains.kotlin.idea.util.JavaParametersBuilder
//import org.jetbrains.kotlin.idea.util.application.runReadAction
//import org.jetbrains.kotlin.psi.KtFile
//import org.jetbrains.kotlin.psi.KtPsiFactory
//import org.jetbrains.kotlin.utils.addToStdlib.safeAs
//import java.io.File
//
//class DivKitPreviewMethod(val project: Project, val file: VirtualFile) {
//
//    var module: Module? = null
//
//    private val psiFile: PsiFile?
//        get() = runReadAction {
//            file.toPsiFile(project)
//        }
//
//    val ktScratchFile: KtFile?
//        get() = psiFile.safeAs()
//
////    val body:
//}
//
//class DivKitPreviewExecutionSession(
//        private val file: DivKitPreviewMethod,
//        private val executor: KtCompilingExecutor
//) {
//    companion object {
//        private const val TIMEOUT_MS = 30000
//    }
//
//    @Volatile
//    private var backgroundProcessIndicator: ProgressIndicator? = null
//
//    fun execute(callback: () -> Unit) {
//        val psiFile = file.ktScratchFile ?: return executor.errorOccurs(
//                KotlinJvmBundle.message("couldn.t.find.ktfile.for.current.editor"),
//                isFatal = true
//        )
//
//        val expressions = file.getExpressions()
//
//        when (val result = runReadAction { DivKitPreviewSourceFileProcessor().process(expressions) }) {
//            executeInBackground(KotlinJvmBundle.message("running.kotlin.scratch")) { indicator ->
//                backgroundProcessIndicator = indicator
//                val modifiedScratchSourceFile = createFileWithLightClassSupport(result, psiFile)
//                tryRunCommandLine(modifiedScratchSourceFile, psiFile, result, callback)
//            }
//        }
//    }
//
//    private fun executeInBackground(@NlsContexts.ProgressTitle title: String, block: (indicator: ProgressIndicator) -> Unit) {
//        object : Task.Backgroundable(file.project, title, true) {
//            override fun run(indicator: ProgressIndicator) = block.invoke(indicator)
//        }.queue()
//    }
//
//    private fun createFileWithLightClassSupport(result: KtScratchSourceFileProcessor.Result.OK, psiFile: KtFile): KtFile =
//            runReadAction { KtPsiFactory(file.project).createFileWithLightClassSupport("tmp.kt", result.code, psiFile) }
//
//    private fun tryRunCommandLine(modifiedScratchSourceFile: KtFile, psiFile: KtFile, result: KtScratchSourceFileProcessor.Result.OK, callback: () -> Unit) {
//        assert(backgroundProcessIndicator != null)
//        try {
//            runCommandLine(
//                    file.project, modifiedScratchSourceFile, file.getExpressions(), psiFile, result,
//                    backgroundProcessIndicator!!, callback
//            )
//        } catch (e: Throwable) {
//            if (e is ControlFlowException) throw e
//            reportError(result, e, psiFile)
//        }
//    }
//
//    fun reportError(result: KtScratchSourceFileProcessor.Result.OK, e: Throwable, psiFile: KtFile) {
//        LOG.printDebugMessage(result.code)
//        executor.errorOccurs(e.message
//                ?: KotlinJvmBundle.message("couldn.t.compile.0", psiFile.name), e, isFatal = true)
//    }
//
//    private fun runCommandLine(
//            project: Project,
//            modifiedScratchSourceFile: KtFile,
//            expressions: List<ScratchExpression>,
//            psiFile: KtFile,
//            result: KtScratchSourceFileProcessor.Result.OK,
//            indicator: ProgressIndicator,
//            callback: () -> Unit
//    ) {
//        val tempDir = DumbService.getInstance(project).runReadActionInSmartMode(Computable {
//            compileFileToTempDir(modifiedScratchSourceFile)
//        })
//
//        try {
//            val (environmentRequest, commandLine) = createCommandLine(psiFile, file.module, result.mainClassName, tempDir.path)
//            val environment = environmentRequest.prepareEnvironment(TargetProgressIndicatorAdapter(indicator))
//
//            val commandLinePresentation = commandLine.getCommandPresentation(environment)
//
//            val processHandler = CapturingProcessHandler(environment.createProcess(commandLine, indicator), null, commandLinePresentation)
//            val executionResult = processHandler.runProcessWithProgressIndicator(indicator, TIMEOUT_MS)
//            executor.parseOutput(executionResult, expressions)
//        } finally {
//            tempDir.delete()
//            callback()
//        }
//    }
//
//    private fun compileFileToTempDir(psiFile: KtFile): File {
//        val tmpDir = FileUtil.createTempDirectory("compile", "scratch")
//        KotlinCompilerIde(psiFile).compileToDirectory(tmpDir)
//        return tmpDir
//    }
//
//    private fun createCommandLine(originalFile: KtFile,
//                                  module: Module?,
//                                  mainClassName: String,
//                                  tempOutDir: String
//    ): Pair<TargetEnvironmentRequest, TargetedCommandLine> {
//        val javaParameters = JavaParametersBuilder(originalFile.project)
//                .withSdkFrom(module, true)
//                .withMainClassName(mainClassName)
//                .build()
//
//        javaParameters.classPath.add(tempOutDir)
//
//        if (module != null) {
//            javaParameters.classPath.addAll(JavaParametersBuilder.getModuleDependencies(module))
//        }
//
//        ScriptConfigurationManager.getInstance(originalFile.project)
//                .getConfiguration(originalFile)?.let {
//                    javaParameters.classPath.addAll(it.dependenciesClassPath.map { f -> f.absolutePath })
//                }
//
//        val wslConfiguration = JavaCommandLineState.checkCreateWslConfiguration(javaParameters.jdk)
//        val request = wslConfiguration?.createEnvironmentRequest(originalFile.project)
//                ?: LocalTargetEnvironmentRequest()
//
//        return request to javaParameters.toCommandLine(request).build()
//    }
//}
