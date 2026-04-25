package com.kangraemin.pictalk.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kangraemin.pictalk.domain.model.AacCategory
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent

private val PTBackground = Color(0xFFFAF7F2)
private val PTInk        = Color(0xFF2A2622)
private val PTCoral      = Color(0xFFFF8A6B)

@CircuitInject(HomeScreen::class, SingletonComponent::class)
@Composable
fun HomeUi(state: HomeScreen.State, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PTBackground)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PTBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Pictalk",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PTInk,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.categories) { category ->
                CategoryTile(category = category, onClick = {
                    state.eventSink(HomeScreen.Event.OnCategorySelected(category.id))
                })
            }
            // AI 카드 만들기 타일
            item {
                AiTile(onClick = { state.eventSink(HomeScreen.Event.OnAiSelected) })
            }
        }
    }
}

@Composable
private fun CategoryTile(category: AacCategory, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArasaacImage(
            symbolId = category.iconSymbolId,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PTInk,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AiTile(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(PTCoral)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "📷", fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI 카드 만들기",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
