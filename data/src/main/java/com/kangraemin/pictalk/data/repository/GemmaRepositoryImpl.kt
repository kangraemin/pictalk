package com.kangraemin.pictalk.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.DownloadState
import com.kangraemin.pictalk.domain.model.GemmaSetupState
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val arasaacRepository: ArasaacRepository,
    private val modelRepository: ModelRepository,
) : GemmaRepository {

    private var llmInference: LlmInference? = null

    private val setupStarted = AtomicBoolean(false)
    private val _setupState = MutableStateFlow<GemmaSetupState>(GemmaSetupState.Idle)
    override val setupState: StateFlow<GemmaSetupState> = _setupState.asStateFlow()

    override fun isReady(): Boolean = llmInference != null

    override suspend fun setup() = withContext(Dispatchers.IO) {
        if (!setupStarted.compareAndSet(false, true)) return@withContext
        runCatching {
            modelRepository.downloadModel().collect { state ->
                when (state) {
                    is DownloadState.Idle -> _setupState.value = GemmaSetupState.Idle
                    is DownloadState.Downloading ->
                        _setupState.value = GemmaSetupState.Downloading(state.progressPercent)
                    is DownloadState.Complete -> {
                        _setupState.value = GemmaSetupState.Initializing
                        val options = LlmInference.LlmInferenceOptions.builder()
                            .setModelPath(modelRepository.modelPath())
                            .setMaxTokens(512).build()
                        llmInference = LlmInference.createFromOptions(context, options)
                        _setupState.value = GemmaSetupState.Ready
                    }
                    is DownloadState.Error ->
                        _setupState.value = GemmaSetupState.Error(state.message)
                }
            }
        }.onFailure {
            _setupState.value = GemmaSetupState.Error(it.message ?: "초기화 실패")
            setupStarted.set(false)
        }
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
            .map { text ->
                val symbol = arasaacRepository.findByKeyword(text)
                AacLabel(
                    text = text,
                    symbolId = symbol?.id,
                    localImagePath = symbol?.localImagePath,
                )
            }
}
