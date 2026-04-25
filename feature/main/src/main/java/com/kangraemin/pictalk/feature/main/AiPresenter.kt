package com.kangraemin.pictalk.feature.main

import android.net.Uri
import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.model.AacLabel
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class AiPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val gemmaRepository: GemmaRepository,
) : Presenter<AiScreen.State> {

    @CircuitInject(AiScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): AiPresenter
    }

    @Composable
    override fun present(): AiScreen.State {
        val scope = rememberCoroutineScope()
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var labels by remember { mutableStateOf<List<AacLabel>>(emptyList()) }
        var isInferring by remember { mutableStateOf(false) }

        return AiScreen.State(
            imageUri = imageUri,
            labels = labels,
            isInferring = isInferring,
        ) { event ->
            when (event) {
                is AiScreen.Event.OnImageSelected -> {
                    imageUri = event.uri
                    labels = emptyList()
                    isInferring = true
                    scope.launch {
                        runCatching { gemmaRepository.suggestLabels(event.uri.toString()) }
                            .onSuccess { labels = it }
                        isInferring = false
                    }
                }
                is AiScreen.Event.OnCardTapped -> Unit  // TTS는 UI에서 처리
                AiScreen.Event.OnBack -> navigator.pop()
            }
        }
    }
}
