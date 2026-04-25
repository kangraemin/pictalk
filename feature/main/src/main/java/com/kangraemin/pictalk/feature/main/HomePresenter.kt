package com.kangraemin.pictalk.feature.main

import androidx.compose.runtime.*
import com.kangraemin.pictalk.domain.repository.ArasaacRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val arasaacRepository: ArasaacRepository,
) : Presenter<HomeScreen.State> {

    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeScreen.State {
        val categories = remember { arasaacRepository.getCategories() }

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
