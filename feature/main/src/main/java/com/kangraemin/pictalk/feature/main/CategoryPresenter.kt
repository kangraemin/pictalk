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

class CategoryPresenter @AssistedInject constructor(
    @Assisted private val screen: CategoryScreen,
    @Assisted private val navigator: Navigator,
    private val arasaacRepository: ArasaacRepository,
) : Presenter<CategoryScreen.State> {

    @CircuitInject(CategoryScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: CategoryScreen, navigator: Navigator): CategoryPresenter
    }

    @Composable
    override fun present(): CategoryScreen.State {
        val category = remember { arasaacRepository.getCategory(screen.categoryId) }

        return CategoryScreen.State(
            category = category,
        ) { event ->
            when (event) {
                is CategoryScreen.Event.OnCardTapped -> Unit  // TTS는 UI 레이어에서 처리
                CategoryScreen.Event.OnBack -> navigator.pop()
            }
        }
    }
}
