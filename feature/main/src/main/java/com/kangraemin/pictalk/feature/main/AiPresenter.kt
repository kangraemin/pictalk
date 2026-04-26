package com.kangraemin.pictalk.feature.main

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        val gemmaSetupState by gemmaRepository.setupState.collectAsState()
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var labels by remember { mutableStateOf<List<AacLabel>>(emptyList()) }
        var sentence by remember { mutableStateOf<List<AacLabel>>(emptyList()) }
        var refinedSentence by remember { mutableStateOf("") }
        var isRefining by remember { mutableStateOf(false) }
        var isInferring by remember { mutableStateOf(false) }

        return AiScreen.State(
            gemmaSetupState = gemmaSetupState,
            imageUri = imageUri,
            labels = labels,
            sentence = sentence,
            refinedSentence = refinedSentence,
            isRefining = isRefining,
            isInferring = isInferring,
        ) { event ->
            when (event) {
                is AiScreen.Event.OnImageSelected -> {
                    imageUri = event.uri
                    labels = emptyList()
                    sentence = emptyList()
                    refinedSentence = ""
                    isInferring = true
                    scope.launch {
                        runCatching { gemmaRepository.suggestLabels(event.uri.toString()) }
                            .onSuccess { labels = it }
                        isInferring = false
                    }
                }
                is AiScreen.Event.OnCardTapped -> sentence = sentence + event.label
                is AiScreen.Event.OnReadSentence -> {
                    if (sentence.isNotEmpty()) {
                        isRefining = true
                        scope.launch {
                            runCatching {
                                gemmaRepository.refineSentence(sentence.map { it.text })
                            }.onSuccess { refined ->
                                refinedSentence = refined
                            }.onFailure {
                                refinedSentence = sentence.joinToString(" ") { it.text }
                            }
                            isRefining = false
                        }
                    }
                }
                is AiScreen.Event.OnClearSentence -> {
                    sentence = emptyList()
                    refinedSentence = ""
                }
                is AiScreen.Event.OnRetryGemmaSetup -> scope.launch { gemmaRepository.setup() }
                AiScreen.Event.OnBack -> navigator.pop()
            }
        }
    }
}
