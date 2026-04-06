package com.kangraemin.pictalk.feature.main

import android.net.Uri
import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.model.DownloadState
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class MainPresenter @AssistedInject constructor(
    private val modelRepository: ModelRepository,
    private val gemmaRepository: GemmaRepository,
) : Presenter<MainScreen.State> {

    @CircuitInject(MainScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(): MainPresenter
    }

    @Composable
    override fun present(): MainScreen.State {
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf<MainScreen.Status>(MainScreen.Status.CheckingModel) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var labels by remember { mutableStateOf<List<AacLabel>>(emptyList()) }
        var isInferring by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { prepareModel { status = it } }

        return MainScreen.State(
            status = status,
            imageUri = imageUri,
            labels = labels,
            isInferring = isInferring,
        ) { event ->
            when (event) {
                is MainScreen.Event.OnRetryDownload -> scope.launch { prepareModel { status = it } }
                is MainScreen.Event.OnImageSelected -> {
                    imageUri = event.uri
                    labels = emptyList()
                    isInferring = true
                    scope.launch {
                        runCatching { gemmaRepository.suggestLabels("a scene from the selected photo") }
                            .onSuccess { labels = it }
                            .onFailure { status = MainScreen.Status.Error("추론 실패: ${it.message}") }
                        isInferring = false
                    }
                }
                is MainScreen.Event.OnCardClicked -> Unit
            }
        }
    }

    private suspend fun prepareModel(onStatus: (MainScreen.Status) -> Unit) {
        modelRepository.downloadModel().collect { state ->
            when (state) {
                is DownloadState.Idle -> onStatus(MainScreen.Status.CheckingModel)
                is DownloadState.Downloading -> onStatus(MainScreen.Status.Downloading(state.progressPercent))
                is DownloadState.Complete -> {
                    onStatus(MainScreen.Status.InitializingModel)
                    runCatching { gemmaRepository.initialize(modelRepository.modelPath()) }
                        .onSuccess { onStatus(MainScreen.Status.Ready("준비 완료. 사진을 선택하세요.")) }
                        .onFailure { onStatus(MainScreen.Status.Error("모델 초기화 실패: ${it.message}")) }
                }
                is DownloadState.Error -> onStatus(MainScreen.Status.Error(state.message))
            }
        }
    }
}
