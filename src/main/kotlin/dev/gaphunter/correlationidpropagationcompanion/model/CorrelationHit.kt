package dev.gaphunter.correlationidpropagationcompanion.model

import com.intellij.psi.PsiElement

/**
 * One handler method that receives a correlation/trace-id header
 * parameter, makes an outbound HTTP call in its own body, but never
 * references that parameter again anywhere in the body -- the concrete,
 * checkable signal that the incoming id is silently dropped instead of
 * forwarded to the downstream call.
 */
data class CorrelationHit(val methodNameElement: PsiElement, val headerParamName: String)
