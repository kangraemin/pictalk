package com.kangraemin.pictalk.feature.main

import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.model.ArasaacDownloadState
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
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
        var status by remember { mutableStateOf<MainScreen.Status>(MainScreen.Status.CheckingSymbols) }

        LaunchedEffect(Unit) {
            if (arasaacRepository.isReady()) {
                navigator.goTo(HomeScreen)
                return@LaunchedEffect
            }
            status = MainScreen.Status.CheckingSymbols
            arasaacRepository.downloadAll().collect { dlState ->
                when (dlState) {
                    is ArasaacDownloadState.DownloadingMetadata ->
                        status = MainScreen.Status.CheckingSymbols
                    is ArasaacDownloadState.DownloadingImages ->
                        status = MainScreen.Status.DownloadingSymbols(
                            dlState.progressPercent, dlState.downloaded, dlState.total)
                    is ArasaacDownloadState.Complete -> navigator.goTo(HomeScreen)
                    is ArasaacDownloadState.Error ->
                        status = MainScreen.Status.Error(dlState.message)
                    else -> Unit
                }
            }
        }

        return MainScreen.State(
            status = status,
        ) { event ->
            when (event) {
                is MainScreen.Event.OnRetryDownload -> scope.launch {
                    if (arasaacRepository.isReady()) {
                        navigator.goTo(HomeScreen)
                    } else {
                        arasaacRepository.downloadAll().collect { dlState ->
                            when (dlState) {
                                is ArasaacDownloadState.DownloadingMetadata ->
                                    status = MainScreen.Status.CheckingSymbols
                                is ArasaacDownloadState.DownloadingImages ->
                                    status = MainScreen.Status.DownloadingSymbols(
                                        dlState.progressPercent, dlState.downloaded, dlState.total)
                                is ArasaacDownloadState.Complete -> navigator.goTo(HomeScreen)
                                is ArasaacDownloadState.Error ->
                                    status = MainScreen.Status.Error(dlState.message)
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}
