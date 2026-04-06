package com.kangraemin.pictalk.feature.main

import android.net.Uri
import com.kangraemin.pictalk.domain.model.AacLabel
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class MainScreenStateTest {

    @Test
    fun `default State has CheckingModel status`() {
        assertTrue(MainScreen.State().status is MainScreen.Status.CheckingModel)
    }

    @Test
    fun `Downloading status carries progressPercent`() {
        assertEquals(42, (MainScreen.Status.Downloading(42)).progressPercent)
    }

    @Test
    fun `State with labels is not empty`() {
        assertEquals(
            2,
            MainScreen.State(labels = listOf(AacLabel("먹어요"), AacLabel("사과"))).labels.size,
        )
    }

    @Test
    fun `OnImageSelected event carries uri`() {
        val uri = mockk<Uri>()
        assertEquals(uri, (MainScreen.Event.OnImageSelected(uri)).uri)
    }
}
