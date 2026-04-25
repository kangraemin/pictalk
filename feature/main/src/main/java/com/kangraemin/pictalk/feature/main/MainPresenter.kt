package com.kangraemin.pictalk.feature.main

import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.model.ArasaacDownloadState
import com.kangraemin.pictalk.domain.model.DownloadState
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.kangraemin.pictalk.domain.repository.ModelRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class MainPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val modelRepository: ModelRepository,
    private val gemmaRepository: GemmaRepository,
    private val arasaacRepository: ArasaacRepository,
) : Presenter<MainScreen.State> {

    @CircuitInject(MainScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): MainPresenter
    }

    @Composable
    override fun present(): MainScreen.State {
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf<MainScreen.Status>(MainScreen.Status.CheckingModel) }

        LaunchedEffect(Unit) { prepareModel { status = it } }

        return MainScreen.State(
            status = status,
        ) { event ->
            when (event) {
                is MainScreen.Event.OnRetryDownload -> scope.launch { prepareModel { status = it } }
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
                        .onSuccess {
                            // ARASAAC 체크
                            if (arasaacRepository.isReady()) {
                                navigator.goTo(HomeScreen)
                            } else {
                                onStatus(MainScreen.Status.CheckingSymbols)
                                arasaacRepository.downloadAll().collect { dlState ->
                                    when (dlState) {
                                        is ArasaacDownloadState.DownloadingMetadata ->
                                            onStatus(MainScreen.Status.CheckingSymbols)
                                        is ArasaacDownloadState.DownloadingImages ->
                                            onStatus(MainScreen.Status.DownloadingSymbols(
                                                dlState.progressPercent,
                                                dlState.downloaded,
                                                dlState.total,
                                            ))
                                        is ArasaacDownloadState.Complete ->
                                            navigator.goTo(HomeScreen)
                                        is ArasaacDownloadState.Error ->
                                            onStatus(MainScreen.Status.Error(dlState.message))
                                        else -> Unit
                                    }
                                }
                            }
                        }
                        .onFailure { onStatus(MainScreen.Status.Error("모델 초기화 실패: ${it.message}")) }
                }
                is DownloadState.Error -> onStatus(MainScreen.Status.Error(state.message))
            }
        }
    }
}
