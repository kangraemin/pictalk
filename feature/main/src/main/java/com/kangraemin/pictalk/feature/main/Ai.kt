package com.kangraemin.pictalk.feature.main

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.kangraemin.pictalk.domain.model.AacLabel
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.Locale

private val PTBackground  = Color(0xFFFAF7F2)
private val PTInk         = Color(0xFF2A2622)
private val PTInkSoft     = Color(0xFF6B655E)
private val PTInkFaint    = Color(0xFFA39C92)
private val PTCoral       = Color(0xFFFF8A6B)
private val PTSky         = Color(0xFF7DB8E8)
private val PTSkyDeep     = Color(0xFF5A9CD4)
private val PTMint        = Color(0xFFBFE6D2)
private val PTLavender    = Color(0xFFD9CFF0)
private val PTPeach       = Color(0xFFFFD4BF)
private val PTButter      = Color(0xFFFFE9A8)
private val PTSkyPale     = Color(0xFFCFE6F8)
private val PTRose        = Color(0xFFF8C9C9)

private val aiCardPastels = listOf(PTRose, PTButter, PTSkyPale, PTMint, PTLavender, PTPeach)

@Composable
fun AACCard(label: AacLabel, index: Int, onTap: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.96f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "scale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth().aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onTap() }
                )
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = aiCardPastels[index % aiCardPastels.size]),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (pressed) BorderStroke(3.dp, PTCoral) else null,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label.text, style = MaterialTheme.typography.titleLarge, color = PTInk)
        }
    }
}

@Composable
fun ScreenReadyEmpty(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(PTBackground)) {
        PTHeader(right = { HeaderMenu() })
        Column(Modifier.fillMaxSize().padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(220.dp)
                    .drawBehind {
                        drawRoundRect(PTPeach, cornerRadius = CornerRadius(24.dp.toPx()),
                            style = Stroke(2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))))
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .clickable { onCamera() },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.align(Alignment.TopStart).offset((-20).dp, (-20).dp)
                    .size(80.dp).background(PTButter.copy(alpha = .6f), CircleShape))
                Box(Modifier.align(Alignment.BottomEnd).offset(18.dp, 18.dp)
                    .size(70.dp).background(PTSkyPale.copy(alpha = .7f), CircleShape))
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(64.dp).background(PTCoral, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center) {
                        Text("📷", fontSize = 28.sp)
                    }
                    Text("사진을 찍어봐요!", style = MaterialTheme.typography.titleMedium, color = PTInk)
                    Text("핍이 카드로 만들어줄 거예요",
                        style = MaterialTheme.typography.bodySmall, color = PTInkSoft)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionPill("📷  카메라", PTCoral, Modifier.weight(1f), onCamera)
                ActionPill("🖼  사진 선택", PTSky, Modifier.weight(1f), onGallery)
            }
            Spacer(Modifier.height(24.dp))
            Text("카드",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                ),
                color = PTInkSoft,
                modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(10.dp))
            val ghostColors = listOf(PTMint, PTLavender, PTPeach, PTButter)
            LazyVerticalGrid(GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(4) { i ->
                    Box(Modifier.fillMaxWidth().aspectRatio(1f)
                        .background(ghostColors[i].copy(alpha = .35f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center) {
                        Text("·", fontSize = 28.sp, color = PTInkFaint.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenReadyActive(
    imageUri: Uri,
    labels: List<AacLabel>,
    isInferring: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onCardTap: (AacLabel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inferTransition = rememberInfiniteTransition(label = "infer")
    val inferSpin by inferTransition.animateFloat(0f, 360f,
        infiniteRepeatable(tween(900, easing = LinearEasing)), label = "inferSpin")

    Column(modifier.fillMaxSize().background(PTBackground)) {
        PTHeader(right = { HeaderMenu() })
        Column(Modifier.fillMaxSize().padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(196.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(4.dp, RoundedCornerShape(24.dp))) {
                AsyncImage(imageUri, null, Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop)
                if (!isInferring && labels.isNotEmpty()) {
                    Row(Modifier.align(Alignment.TopStart).padding(12.dp)
                        .background(Color.White.copy(alpha = .92f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(6.dp).background(Color(0xFF41C480), CircleShape))
                        Text("${labels.size}개 단어 찾음",
                            style = MaterialTheme.typography.bodySmall, color = PTInk)
                    }
                }
                Row(Modifier.align(Alignment.BottomEnd).padding(12.dp)
                    .background(Color.White.copy(alpha = .92f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable { onCamera() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🔄", fontSize = 12.sp)
                    Text("다시 찍기", style = MaterialTheme.typography.bodySmall, color = PTInk)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPillCompact("📷  카메라", PTCoral, Modifier.weight(1f), onCamera)
                ActionPillCompact("🖼  사진 선택", PTSkyDeep, Modifier.weight(1f), onGallery)
            }
            Spacer(Modifier.height(14.dp))
            if (isInferring) {
                Column(Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(80.dp).graphicsLayer { rotationZ = inferSpin }
                        .drawBehind {
                            val r = size.minDimension / 2f
                            val sw = 5.dp.toPx()
                            val stroke = Stroke(sw, cap = StrokeCap.Butt)
                            drawCircle(PTPeach, r, style = stroke)
                            drawArc(
                                color = PTCoral,
                                startAngle = -90f, sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(sw / 2, sw / 2),
                                size = Size(size.width - sw, size.height - sw),
                                style = stroke,
                            )
                        })
                    Text("핍이 보고 있어요…",
                        style = MaterialTheme.typography.titleMedium, color = PTInk)
                }
            } else {
                LazyVerticalGrid(GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(labels) { i, label ->
                        AACCard(label, i) { onCardTap(label) }
                    }
                }
            }
        }
    }
}

@CircuitInject(AiScreen::class, SingletonComponent::class)
@Composable
fun AiUi(state: AiScreen.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    DisposableEffect(Unit) {
        val t = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.KOREAN
        }
        tts = t
        onDispose { t.shutdown() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { state.eventSink(AiScreen.Event.OnImageSelected(it)) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { state.eventSink(AiScreen.Event.OnImageSelected(it)) }
    }

    val onCamera: () -> Unit = {
        val tmp = File(context.cacheDir, "tmp_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tmp)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }
    val onGallery: () -> Unit = { galleryLauncher.launch("image/*") }

    if (state.imageUri != null) {
        ScreenReadyActive(
            imageUri = state.imageUri,
            labels = state.labels,
            isInferring = state.isInferring,
            onCamera = onCamera,
            onGallery = onGallery,
            onCardTap = { label ->
                state.eventSink(AiScreen.Event.OnCardTapped(label))
                tts?.speak(label.text, TextToSpeech.QUEUE_FLUSH, null, null)
            },
            modifier = modifier,
        )
    } else {
        ScreenReadyEmpty(
            onCamera = onCamera,
            onGallery = onGallery,
            modifier = modifier,
        )
    }
}
