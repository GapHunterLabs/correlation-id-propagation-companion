package dev.gaphunter.correlationidpropagationcompanion.detect

import com.intellij.psi.PsiFile
import dev.gaphunter.correlationidpropagationcompanion.model.CorrelationHit
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaCorrelationFinder]. */
object KotlinCorrelationFinder {

    fun findAll(file: PsiFile): List<CorrelationHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<CorrelationHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                val param = correlationHeaderParam(function) ?: return
                val body = function.bodyExpression ?: return
                if (!CorrelationSignals.bodyMakesOutboundHttpCall(body.text)) return
                if (referencedElsewhere(body, param)) return
                val nameIdentifier = function.nameIdentifier ?: return
                hits += CorrelationHit(nameIdentifier, param.name ?: return)
            }
        })
        return hits
    }

    private fun correlationHeaderParam(function: KtNamedFunction): KtParameter? {
        for (param in function.valueParameters) {
            val annotation = param.annotationEntries.firstOrNull { it.shortName?.asString() == "RequestHeader" } ?: continue
            val named = annotation.valueArguments.firstOrNull {
                val argName = it.getArgumentName()?.asName?.asString()
                argName == "value" || argName == "name"
            }
            val positional = annotation.valueArguments.firstOrNull { it.getArgumentName() == null }
            val declaredText = (named ?: positional)?.getArgumentExpression()?.text
            val headerName = declaredText?.trim('"') ?: (param.name ?: continue)
            if (CorrelationSignals.headerNameLooksLikeCorrelationId(headerName)) return param
        }
        return null
    }

    /** True if the parameter's identifier is referenced anywhere in the body other than as a declaration. */
    private fun referencedElsewhere(body: KtExpression, param: KtParameter): Boolean {
        val paramName = param.name ?: return false
        var found = false
        body.accept(object : KtTreeVisitorVoid() {
            override fun visitReferenceExpression(expression: KtReferenceExpression) {
                super.visitReferenceExpression(expression)
                if (found) return
                if (expression.text == paramName) found = true
            }
        })
        return found
    }
}
