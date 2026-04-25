package com.kangraemin.pictalk.feature.main

import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

@CircuitInject(MainScreen::class, SingletonComponent::class)
@Composable
fun Main(state: MainScreen.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) { tts = TextToSpeech(context) { }; tts?.language = Locale.KOREAN }
    DisposableEffect(Unit) { onDispose { tts?.shutdown() } }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { state.eventSink(MainScreen.Event.OnImageSelected(it)) }
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { state.eventSink(MainScreen.Event.OnImageSelected(it)) }
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Pictalk", style = MaterialTheme.typography.headlineLarge)

            when (val s = state.status) {
                is MainScreen.Status.CheckingModel, MainScreen.Status.InitializingModel -> {
                    CircularProgressIndicator()
                    Text(
                        if (s is MainScreen.Status.InitializingModel) "모델 초기화 중..." else "확인 중...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is MainScreen.Status.Downloading -> {
                    Text("모델 다운로드 중 ${s.progressPercent}%", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { s.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Gemma 4 E4B (~2.4GB) · Wi-Fi 권장",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is MainScreen.Status.Ready -> {
                    Text(s.message, style = MaterialTheme.typography.bodyMedium)
                    state.imageUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                val file = File.createTempFile("camera_", ".jpg", context.externalCacheDir)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file,
                                )
                                cameraUri = uri
                                takePhoto.launch(uri)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isInferring,
                        ) { Text("카메라") }
                        Button(
                            onClick = { pickImage.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isInferring,
                        ) { Text("사진 선택") }
                    }
                    if (state.isInferring) CircularProgressIndicator()
                    if (state.labels.isNotEmpty()) {
                        Text("AAC 카드", style = MaterialTheme.typography.titleMedium)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.labels, key = { it.text }) { label ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable {
                                        tts?.speak(label.text, TextToSpeech.QUEUE_FLUSH, null, label.text)
                                        state.eventSink(MainScreen.Event.OnCardClicked(label))
                                    },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        Text(label.text, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                is MainScreen.Status.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { state.eventSink(MainScreen.Event.OnRetryDownload) }) {
                        Text("다시 시도")
                    }
                }
            }
        }
    }
}
