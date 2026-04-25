package com.kangraemin.pictalk.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── PT Design Tokens ───────────────────────────────────────
val PTBackground  = Color(0xFFFAF7F2)
val PTSurface     = Color(0xFFFFFFFF)
val PTInk         = Color(0xFF2A2622)
val PTInkSoft     = Color(0xFF6B655E)
val PTInkFaint    = Color(0xFFA39C92)
val PTCoral       = Color(0xFFFF8A6B)
val PTCoralDeep   = Color(0xFFE56A4D)
val PTSky         = Color(0xFF7DB8E8)
val PTSkyDeep     = Color(0xFF5A9CD4)
val PTMint        = Color(0xFFBFE6D2)
val PTLavender    = Color(0xFFD9CFF0)
val PTPeach       = Color(0xFFFFD4BF)
val PTButter      = Color(0xFFFFE9A8)
val PTSkyPale     = Color(0xFFCFE6F8)
val PTRose        = Color(0xFFF8C9C9)
val PTErrorInk    = Color(0xFFC24E2E)

private val AppColorScheme = lightColorScheme(
    background    = PTBackground,
    surface       = PTSurface,
    primary       = PTCoral,
    onPrimary     = Color.White,
    secondary     = PTSky,
    onSecondary   = Color.White,
    error         = PTErrorInk,
    onBackground  = PTInk,
    onSurface     = PTInk,
)

private val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Black,     fontSize = 22.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 10.sp, letterSpacing = 0.8.sp),
)

@Composable
fun PictalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, typography = AppTypography, content = content)
}
