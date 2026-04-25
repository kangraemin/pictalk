package com.kangraemin.pictalk.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ArasaacImage(
    symbolId: Int?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    if (symbolId != null) {
        val imageFile = File(LocalContext.current.filesDir, "arasaac/images/$symbolId.png")
        AsyncImage(
            model = imageFile,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Box(modifier.background(Color(0xFFCFE6F8)))
    }
}
