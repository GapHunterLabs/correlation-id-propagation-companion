package dev.gaphunter.correlationidpropagationcompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReferenceExpression
import dev.gaphunter.correlationidpropagationcompanion.model.CorrelationHit

/**
 * Finds Java Spring MVC handler methods that receive a `@RequestHeader`
 * parameter carrying a correlation/trace id, make an outbound HTTP call
 * somewhere in their own body, but never reference that parameter again
 * anywhere in the body -- the id is captured and then silently dropped
 * instead of being forwarded to the downstream call.
 */
object JavaCorrelationFinder {

    fun findAll(file: PsiFile): List<CorrelationHit> {
        val hits = mutableListOf<CorrelationHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                val param = correlationHeaderParam(method) ?: return
                val body = method.body ?: return
                if (!CorrelationSignals.bodyMakesOutboundHttpCall(body.text)) return
                if (referencedElsewhere(body, param)) return
                val nameIdentifier = method.nameIdentifier ?: return
                hits += CorrelationHit(nameIdentifier, param.name)
            }
        })
        return hits
    }

    private fun correlationHeaderParam(method: PsiMethod): PsiParameter? {
        for (param in method.parameterList.parameters) {
            val annotation = param.modifierList?.annotations
                ?.firstOrNull { it.nameReferenceElement?.referenceName == "RequestHeader" }
                ?: continue
            val declaredName = (annotation.findAttributeValue("value")
                ?: annotation.findAttributeValue("name")) as? PsiLiteralExpression
            val headerName = declaredName?.value as? String ?: param.name
            if (CorrelationSignals.headerNameLooksLikeCorrelationId(headerName)) return param
        }
        return null
    }

    /** True if the parameter's identifier is referenced anywhere in the body other than as a declaration. */
    private fun referencedElsewhere(body: com.intellij.psi.PsiCodeBlock, param: PsiParameter): Boolean {
        var found = false
        body.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                super.visitReferenceExpression(expression)
                if (found) return
                if (expression.referenceName == param.name) found = true
            }
        })
        return found
    }
}
