package com.kangraemin.pictalk.feature.main

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.kangraemin.pictalk.domain.model.AacLabel
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.Locale

// PT Design Tokens — locally defined (app module is not a dependency of feature:main)
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

enum class PipMood { Happy, Sad, Sleepy }

@Composable
fun Pip(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = PTCoral,
    mood: PipMood = PipMood.Happy,
    wiggle: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pip")
    // pipWiggle: CSS rotate(-2deg)↔rotate(2deg), translateY(0)↔translateY(-4px), 2.6s cycle
    // → tween(1300) RepeatMode.Reverse = 1.3s per direction = 2.6s total ✅
    val wiggleAngle by if (wiggle) infiniteTransition.animateFloat(
        -2f, 2f,   // ← -2°↔+2° (CSS 원본)
        infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wiggle"
    ) else remember { mutableFloatStateOf(0f) }
    val offsetY by if (wiggle) infiniteTransition.animateFloat(
        0f, -4f,
        infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "offsetY"
    ) else remember { mutableFloatStateOf(0f) }

    Canvas(modifier = modifier.size(size).graphicsLayer {
        rotationZ = wiggleAngle; translationY = offsetY
    }) {
        val w = size.toPx(); val h = size.toPx()
        // Blob body — organic cubic bezier
        val blob = Path().apply {
            moveTo(w * .5f, h * .04f)
            cubicTo(w * .82f, h * .04f, w * .96f, h * .22f, w * .96f, h * .44f)
            cubicTo(w * .96f, h * .72f, w * .80f, h * .96f, w * .5f, h * .96f)
            cubicTo(w * .20f, h * .96f, w * .04f, h * .78f, w * .04f, h * .56f)
            cubicTo(w * .04f, h * .22f, w * .22f, h * .04f, w * .5f, h * .04f)
            close()
        }
        drawPath(blob, color)
        // Cheeks
        drawCircle(Color(0x55FFC8B4), w * .09f, Offset(w * .23f, h * .58f))
        drawCircle(Color(0x55FFC8B4), w * .09f, Offset(w * .77f, h * .58f))
        // Eyes
        val eyeY = if (mood == PipMood.Sleepy) h * .46f else h * .40f
        val eyeH = if (mood == PipMood.Sleepy) 2f else 8f
        for (eyeX in listOf(w * .28f, w * .64f)) {
            drawRoundRect(Color(0xFF2A2622.toInt()), Offset(eyeX, eyeY),
                Size(8f, eyeH), CornerRadius(4f))
        }
        // Mouth
        val mouthPath = Path()
        when (mood) {
            PipMood.Happy -> {
                mouthPath.moveTo(w * .42f, h * .60f)
                mouthPath.cubicTo(w * .43f, h * .66f, w * .57f, h * .66f, w * .58f, h * .60f)
                drawPath(mouthPath, Color(0xFF2A2622.toInt()),
                    style = Stroke(2.5f, cap = StrokeCap.Round))
            }
            PipMood.Sad -> {
                mouthPath.moveTo(w * .42f, h * .66f)
                mouthPath.cubicTo(w * .43f, h * .60f, w * .57f, h * .60f, w * .58f, h * .66f)
                drawPath(mouthPath, Color(0xFF2A2622.toInt()),
                    style = Stroke(2.5f, cap = StrokeCap.Round))
            }
            PipMood.Sleepy -> drawRoundRect(Color(0xFF2A2622.toInt()),
                Offset(w * .44f, h * .63f), Size(w * .12f, 3f), CornerRadius(3f))
        }
    }
}

val cardPastels = listOf(PTRose, PTButter, PTSkyPale, PTMint, PTLavender, PTPeach)

@Composable
fun PTHeader(right: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 로고 사각형 (눈 2개 + 미소)
            Box(Modifier.size(28.dp).background(PTCoral, RoundedCornerShape(9.dp))
                .shadow(2.dp, RoundedCornerShape(9.dp))) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White, 2.5f, Offset(size.width * .32f, size.height * .40f))
                    drawCircle(Color.White, 2.5f, Offset(size.width * .68f, size.height * .40f))
                    val smile = Path().apply {
                        moveTo(size.width * .35f, size.height * .63f)
                        cubicTo(size.width * .36f, size.height * .72f,
                                size.width * .64f, size.height * .72f,
                                size.width * .65f, size.height * .63f)
                    }
                    drawPath(smile, Color.White, style = Stroke(1.8f, cap = StrokeCap.Round))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("pictalk", style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp, letterSpacing = (-0.3).sp))
        }
        right?.invoke()
    }
}

// HeaderMenu — Ready 화면의 PTHeader right 슬롯에 들어가는 hamburger 버튼
@Composable
fun HeaderMenu() {
    Box(
        Modifier.size(40.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .background(Color.White, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) {
                Box(Modifier.width(18.dp).height(2.5.dp)
                    .background(PTInk, RoundedCornerShape(2.dp)))
            }
        }
    }
}

// 큰 pill 버튼 (ReadyEmpty 화면)
@Composable
fun ActionPill(label: String, bg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick, modifier.height(56.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

// compact pill 버튼 (ReadyActive 화면) — 흰 배경 + accent 컬러 텍스트
@Composable
fun ActionPillCompact(label: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick, modifier.height(44.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp) {
        Box(Modifier.fillMaxSize().padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
fun AACCard(label: AacLabel, index: Int, onTap: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    // CSS: scale(0.96) active, cubic-bezier(0.34, 1.56, 0.64, 1) → overshoot spring
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
        colors = CardDefaults.cardColors(containerColor = cardPastels[index % cardPastels.size]),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (pressed) BorderStroke(3.dp, PTCoral) else null,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label.text, style = MaterialTheme.typography.titleLarge, color = PTInk)
        }
    }
}

@Composable
fun ScreenChecking(isInitializing: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "orbit")
    val spin by transition.animateFloat(0f, 360f,
        infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "spin")
    val spinRev by transition.animateFloat(0f, -360f,
        infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "spinRev")

    // padding: '0 32px' — horizontal 32dp
    Column(Modifier.fillMaxSize().background(PTBackground)
        .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = spin }) {
                drawCircle(PTPeach, size.minDimension / 2,
                    style = Stroke(3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))))
            }
            Canvas(Modifier.size(152.dp).graphicsLayer { rotationZ = spinRev }) {
                drawCircle(PTSkyPale, size.minDimension / 2,
                    style = Stroke(2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))))
            }
            Pip(size = 110.dp, wiggle = true)
        }
        Spacer(Modifier.height(36.dp))
        Text(if (isInitializing) "모델 초기화 중이에요" else "준비 중이에요",
            style = MaterialTheme.typography.headlineMedium, color = PTInk)
        Spacer(Modifier.height(8.dp))
        Text("핍이 카드를 찾고 있어요 ✨",
            style = MaterialTheme.typography.bodyLarge, color = PTInkSoft)
        Spacer(Modifier.height(32.dp))
        // dotPulse: CSS 1.4s 전체 사이클 = 방향당 700ms
        // tween(700) RepeatMode.Reverse → 700ms + 700ms reverse = 1.4s ✅
        val dotScales = (0..2).map { i ->
            transition.animateFloat(0.7f, 1.15f,
                infiniteRepeatable(tween(700, delayMillis = i * 180, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse), label = "dotScale$i")
        }
        val dotAlphas = (0..2).map { i ->
            transition.animateFloat(0.5f, 1.0f,
                infiniteRepeatable(tween(700, delayMillis = i * 180, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse), label = "dotAlpha$i")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..2).forEach { i ->
                Box(Modifier.size(10.dp)
                    .graphicsLayer {
                        scaleX = dotScales[i].value
                        scaleY = dotScales[i].value
                        alpha = dotAlphas[i].value
                    }
                    .background(PTCoral, CircleShape))
            }
        }
    }
}

@Composable
fun ScreenDownloading(progressPercent: Int) {
    val progress = progressPercent / 100f
    // CSS: transition: 'stroke-dashoffset 0.6s ease' — progress 변경 시 smooth 0.6s 전환
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "downloadArc"
    )
    val downloaded = "%.1f".format(progress * 2.4f)
    Column(Modifier.fillMaxSize().background(PTBackground)) {
        PTHeader()
        // padding: '8px 28px 24px'
        Column(Modifier.fillMaxSize().padding(top = 8.dp, start = 28.dp, end = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(220.dp).padding(top = 14.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = 78.dp.toPx()
                    val stroke = Stroke(14.dp.toPx(), cap = StrokeCap.Round)
                    val tl = Offset(center.x - r, center.y - r)
                    drawCircle(PTPeach.copy(alpha = .55f), r, style = stroke)
                    // animatedProgress 사용 → 0.6s smooth transition
                    drawArc(PTCoral, -90f, 360f * animatedProgress, false, tl, Size(r*2, r*2), style = stroke)
                }
                // 4개 Sparkle — 디자인 원본 위치 반영
                Text("✨", Modifier.align(Alignment.TopStart).offset(16.dp, 18.dp), fontSize = 18.sp)
                Text("⭐", Modifier.align(Alignment.TopEnd).offset((-22).dp, 30.dp), fontSize = 14.sp)
                Text("✨", Modifier.align(Alignment.BottomEnd).offset((-10).dp, (-36).dp), fontSize = 16.sp)
                Text("⭐", Modifier.align(Alignment.BottomStart).offset(22.dp, (-22).dp), fontSize = 14.sp)
                Pip(size = 120.dp, wiggle = true)
            }
            Spacer(Modifier.height(22.dp))
            Text("앱을 준비하고 있어요!", style = MaterialTheme.typography.headlineMedium, color = PTInk)
            Spacer(Modifier.height(6.dp))
            Text("조금만 기다려줘요 🌟", style = MaterialTheme.typography.bodyLarge, color = PTInkSoft)
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$progressPercent", style = MaterialTheme.typography.displayLarge,
                        color = PTCoral)
                    Text("%", style = MaterialTheme.typography.headlineMedium, color = PTCoral)
                }
                Text("${downloaded}GB / 2.4GB",
                    style = MaterialTheme.typography.bodySmall, color = PTInkSoft)
            }
            Spacer(Modifier.height(18.dp))
            // Wi-Fi tip card
            Row(Modifier.fillMaxWidth().background(PTSkyPale, RoundedCornerShape(14.dp))
                .padding(12.dp, 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(Color.White, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Text("📶", fontSize = 16.sp)
                }
                Column {
                    Text("Wi-Fi 연결 시 더 빨라요!",
                        style = MaterialTheme.typography.bodySmall, color = PTSkyDeep)
                    Text("한 번만 받으면 끝이에요.",
                        style = MaterialTheme.typography.bodySmall, color = PTInkSoft)
                }
            }
        }
    }
}

@Composable
fun ScreenReadyEmpty(onCamera: () -> Unit, onGallery: () -> Unit) {
    Column(Modifier.fillMaxSize().background(PTBackground)) {
        PTHeader(right = { HeaderMenu() })
        // padding: '4px 16px 8px'
        Column(Modifier.fillMaxSize().padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Spacer(Modifier.height(8.dp))  // marginTop: 8 on photo zone
            // 점선 사진존 — outer draws dashed border, inner clips corner bubbles
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
                // 코너 파스텔 장식 (overflow hidden — clip 처리됨)
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
            // 2 pill 버튼
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionPill("📷  카메라", PTCoral, Modifier.weight(1f), onCamera)
                ActionPill("🖼  사진 선택", PTSky, Modifier.weight(1f), onGallery)
            }
            Spacer(Modifier.height(24.dp))
            // "카드": fontSize: 13, fontWeight: 800, letterSpacing: 0.4 (디자인 원본)
            Text("카드",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                ),
                color = PTInkSoft,
                modifier = Modifier.padding(start = 4.dp))
            Spacer(Modifier.height(10.dp))
            // Ghost 카드 그리드 — 정확한 4색 [mint, lavender, peach, butter] (디자인 원본)
            // CSS opacity: 0.35 → .copy(alpha=0.35f) on background color
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
) {
    Column(Modifier.fillMaxSize().background(PTBackground)) {
        PTHeader(right = { HeaderMenu() })
        // padding: '4px 16px 8px'
        Column(Modifier.fillMaxSize().padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)) {
            Spacer(Modifier.height(8.dp))
            // 사진 미리보기 — marginTop: 8 반영
            Box(Modifier.fillMaxWidth().height(196.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(4.dp, RoundedCornerShape(24.dp))) {
                AsyncImage(imageUri, null, Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop)
                // "N개 단어 찾음" pill
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
                // 다시 찍기 버튼
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
            // compact 버튼
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPillCompact("📷  카메라", PTCoral, Modifier.weight(1f), onCamera)
                ActionPillCompact("🖼  사진 선택", PTSkyDeep, Modifier.weight(1f), onGallery)
            }
            Spacer(Modifier.height(14.dp))
            if (isInferring) {
                // "핍이 보고 있어요…" 오버레이 — 기본 CI 대신 디자인 일치
                val inferTransition = rememberInfiniteTransition(label = "infer")
                val inferSpin by inferTransition.animateFloat(0f, 360f,
                    infiniteRepeatable(tween(900, easing = LinearEasing)), label = "inferSpin")
                // CSS: border: 5px solid PTPeach; borderTopColor: PTCoral → 상단 90°만 coral
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
                // weight(1f) — 카드 그리드가 나머지 공간을 채우고 스크롤
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

@Composable
fun ScreenError(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().background(PTBackground)) {
        PTHeader()
        Column(Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                // opacity: 0.7 반영
                Text("💧", Modifier.align(Alignment.TopStart).offset(22.dp, 20.dp).alpha(0.7f), fontSize = 16.sp)
                Text("💧", Modifier.align(Alignment.TopEnd).offset((-26).dp, 30.dp).alpha(0.7f), fontSize = 14.sp)
                Pip(size = 120.dp, color = Color(0xFFC7BBA8), mood = PipMood.Sad)
            }
            Spacer(Modifier.height(28.dp))
            Text("앗, 잠깐만요",
                style = MaterialTheme.typography.headlineMedium, color = PTInk)
            Spacer(Modifier.height(10.dp))
            // maxWidth: 260 → widthIn(max = 260.dp)
            Text("사진을 카드로 만들지 못했어요.\n다시 한번 시도해볼까요?",
                style = MaterialTheme.typography.bodyLarge, color = PTInkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 260.dp))
            Spacer(Modifier.height(28.dp))
            // padding: '16px 36px' → contentPadding으로 재현
            Button(onClick = onRetry,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PTCoral),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)) {
                Text("🔄  다시 시도하기",
                    style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            // 오류 코드 — 디자인 원본: "오류 코드 · NET_TIMEOUT"
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (message.isNotBlank()) "오류 코드 · ${message.take(20)}" else "오류 코드 · UNKNOWN",
                style = MaterialTheme.typography.bodySmall, color = PTInkFaint,
            )
        }
    }
}

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

    fun launchCamera() {
        val file = File.createTempFile("camera_", ".jpg", context.externalCacheDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        takePhoto.launch(uri)
    }

    Scaffold(modifier = modifier, containerColor = PTBackground) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = state.status) {
                is MainScreen.Status.CheckingModel     -> ScreenChecking(false)
                is MainScreen.Status.InitializingModel -> ScreenChecking(true)
                is MainScreen.Status.Downloading       -> ScreenDownloading(s.progressPercent)
                is MainScreen.Status.Ready -> {
                    if (state.imageUri != null) {
                        ScreenReadyActive(
                            imageUri = state.imageUri,
                            labels = state.labels,
                            isInferring = state.isInferring,
                            onCamera = { launchCamera() },
                            onGallery = { pickImage.launch("image/*") },
                            onCardTap = { label ->
                                tts?.speak(label.text, TextToSpeech.QUEUE_FLUSH, null, label.text)
                                state.eventSink(MainScreen.Event.OnCardClicked(label))
                            },
                        )
                    } else {
                        ScreenReadyEmpty(
                            onCamera = { launchCamera() },
                            onGallery = { pickImage.launch("image/*") },
                        )
                    }
                }
                is MainScreen.Status.Error -> ScreenError(s.message) {
                    state.eventSink(MainScreen.Event.OnRetryDownload)
                }
            }
        }
    }
}
