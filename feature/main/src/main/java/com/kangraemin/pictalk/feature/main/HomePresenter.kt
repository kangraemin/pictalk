package com.kangraemin.pictalk.feature.main

import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.kangraemin.pictalk.domain.repository.GemmaRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val arasaacRepository: ArasaacRepository,
    private val gemmaRepository: GemmaRepository,
) : Presenter<HomeScreen.State> {

    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeScreen.State {
        val scope = rememberCoroutineScope()
        val categories = remember { arasaacRepository.getCategories() }

        LaunchedEffect(Unit) {
            scope.launch { gemmaRepository.setup() }
        }

        return HomeScreen.State(
            categories = categories,
        ) { event ->
            when (event) {
                is HomeScreen.Event.OnCategorySelected ->
                    navigator.goTo(CategoryScreen(event.categoryId))
                HomeScreen.Event.OnAiSelected ->
                    navigator.goTo(AiScreen)
            }
        }
    }
}
