package dev.gaphunter.correlationidpropagationcompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.correlationidpropagationcompanion.detect.JavaCorrelationFinder
import dev.gaphunter.correlationidpropagationcompanion.detect.KotlinCorrelationFinder
import dev.gaphunter.correlationidpropagationcompanion.model.CorrelationHit
import dev.gaphunter.correlationidpropagationcompanion.review.ReviewPrompt

class DroppedCorrelationIdLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Dropped correlation/trace id"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaCorrelationFinder.findAll(file)
            "kotlin" -> KotlinCorrelationFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.methodNameElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: CorrelationHit): LineMarkerInfo<PsiElement> {
        val tooltip = "This method receives \"${hit.headerParamName}\" and makes an outbound HTTP call, " +
            "but never references \"${hit.headerParamName}\" again -- the correlation/trace id looks dropped, not forwarded"
        return LineMarkerInfo(
            hit.methodNameElement,
            hit.methodNameElement.textRange,
            CorrelationIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
