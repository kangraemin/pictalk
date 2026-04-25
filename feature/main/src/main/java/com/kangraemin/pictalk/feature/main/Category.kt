package com.kangraemin.pictalk.feature.main

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.util.Locale

private val PTBackground = Color(0xFFFAF7F2)
private val PTInk        = Color(0xFF2A2622)
private val PTInkSoft    = Color(0xFF6B655E)

@CircuitInject(CategoryScreen::class, SingletonComponent::class)
@Composable
fun CategoryUi(state: CategoryScreen.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
            }
        }
        tts = t
        onDispose { t.shutdown() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PTBackground)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.IconButton(onClick = { state.eventSink(CategoryScreen.Event.OnBack) }) {
                Text(text = "←", fontSize = 24.sp, color = PTInk)
            }
            Text(
                text = state.category?.name ?: "",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PTInk,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        val labels = state.category?.labels ?: emptyList()
        if (labels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "카드가 없어요", color = PTInkSoft, fontSize = 16.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(labels) { label ->
                    AacCard(
                        text = label.text,
                        symbolId = label.symbolId,
                        onClick = {
                            state.eventSink(CategoryScreen.Event.OnCardTapped(label))
                            tts?.speak(label.text, TextToSpeech.QUEUE_FLUSH, null, null)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AacCard(text: String, symbolId: Int?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArasaacImage(
            symbolId = symbolId,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PTInk,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}
