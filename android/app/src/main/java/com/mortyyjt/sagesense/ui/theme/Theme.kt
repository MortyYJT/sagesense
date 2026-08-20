package com.mortyyjt.sagesense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SageNavy = Color(0xFF11146B)
val SageBlue = Color(0xFF4E89F5)
val SageSky = Color(0xFFDDEBFF)
val SageBackground = Color(0xFFF6F8FF)
val SageRed = Color(0xFFA61E35)
val SageAmber = Color(0xFF9A5B00)
val SageGreen = Color(0xFF18794E)

private val SageColors = lightColorScheme(
    primary = SageNavy,
    onPrimary = Color.White,
    primaryContainer = SageSky,
    onPrimaryContainer = Color(0xFF06083B),
    secondary = SageBlue,
    error = SageRed,
    background = SageBackground,
    surface = Color.White,
    onSurface = Color(0xFF161824),
    outline = Color(0xFF626575),
)

private val SageTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 19.sp, lineHeight = 27.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 17.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
)

@Composable
fun SageSenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SageColors, typography = SageTypography, content = content)
}
