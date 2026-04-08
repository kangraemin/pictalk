package com.kangraemin.pictalk.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : GemmaRepository {

    private var llmInference: LlmInference? = null

    override fun isReady(): Boolean = llmInference != null

    override suspend fun initialize(modelPath: String) = withContext(Dispatchers.IO) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(512)
            .build()
        llmInference = LlmInference.createFromOptions(context, options)
    }

    override suspend fun suggestLabels(imageDescription: String): List<AacLabel> = withContext(Dispatchers.IO) {
        val inference = llmInference ?: error("GemmaRepository not initialized")
        val bitmap = decodeUri(imageDescription) ?: error("이미지 디코딩 실패: $imageDescription")
        val resized = resizeBitmap(bitmap, maxSize = 512)
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder().build()
        val session = LlmInferenceSession.createFromOptions(inference, sessionOptions)
        session.addQueryChunk(buildPrompt())
        session.addImage(BitmapImageBuilder(resized).build())
        val raw = session.generateResponse()
        session.close()
        parseLabels(raw)
    }

    internal fun buildPrompt(): String = """
        <start_of_turn>user
        You are an AAC assistant for children with autism.
        Look at this image and output 4-6 simple Korean AAC card labels.
        Labels should be words or short phrases a child might want to communicate about this scene.
        Respond ONLY with a comma-separated list. No explanation.
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()

    private fun decodeUri(uriString: String): Bitmap? = runCatching {
        val uri = Uri.parse(uriString)
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        if (w <= maxSize && h <= maxSize) return bitmap
        val scale = maxSize.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), true)
    }

    internal fun parseLabels(raw: String): List<AacLabel> =
        raw.split(",")
            .map { it.trim().trimEnd('.') }
            .filter { it.isNotEmpty() && it.length <= 10 }
            .take(6)
            .map { AacLabel(it) }
}
