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

    override suspend fun refineSentence(labels: List<String>): String = withContext(Dispatchers.IO) {
        val inference = llmInference ?: error("GemmaRepository not initialized")
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder().build()
        val session = LlmInferenceSession.createFromOptions(inference, sessionOptions)
        session.addQueryChunk(buildRefinementPrompt(labels))
        val result = session.generateResponse()
        session.close()
        result.trim()
    }

    internal fun buildRefinementPrompt(labels: List<String>): String = """
        <start_of_turn>user
        다음 AAC 카드 단어들을 자연스러운 한국어 문장 하나로 만들어 주세요.
        단어: ${labels.joinToString(", ")}
        규칙: 짧고 자연스럽게. 문장 하나만 출력. 설명 없이.
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()

    internal fun buildPrompt(): String = """
        <start_of_turn>user
        당신은 자폐·발달장애 아동을 위한 AAC(보완대체의사소통) 전문가입니다.
        이 사진 속 상황을 분석하고, 이 상황에 있는 AAC 사용자가 표현하고 싶을 말 4~6개를 한국어로 추천하세요.
        사진에 보이는 것을 나열하지 말고, 이 상황에서 의사소통에 필요한 말을 추천하세요.
        예시: 병원이면 "아파요, 기다려요, 물 주세요, 화장실" / 식당이면 "주세요, 맛있어요, 더, 배고파요"
        반드시 쉼표로 구분된 한국어 단어·짧은 문장만 출력하세요. 설명 없이.
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
