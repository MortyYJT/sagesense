package com.mortyyjt.sagesense.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String): ThemeMode = entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

val SageNavy = Color(0xFF11146B)
val SageBlue = Color(0xFF4E89F5)
val SageSky = Color(0xFFDDEBFF)
val SageBackground = Color(0xFFF6F8FF)
val SageRed = Color(0xFFA61E35)
val SageAmber = Color(0xFF9A5B00)
val SageGreen = Color(0xFF18794E)

private val SageLightColors = lightColorScheme(
    primary = SageNavy,
    onPrimary = Color.White,
    primaryContainer = SageSky,
    onPrimaryContainer = Color(0xFF06083B),
    secondary = SageBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E7FF),
    onSecondaryContainer = Color(0xFF102B53),
    error = SageRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDADD),
    onErrorContainer = Color(0xFF41000B),
    background = SageBackground,
    onBackground = Color(0xFF161824),
    surface = Color.White,
    onSurface = Color(0xFF161824),
    surfaceVariant = Color(0xFFE4E7F1),
    onSurfaceVariant = Color(0xFF454752),
    outline = Color(0xFF626575),
)

private val SageDarkColors = darkColorScheme(
    primary = Color(0xFFBCC3FF),
    onPrimary = Color(0xFF14185F),
    primaryContainer = Color(0xFF30357D),
    onPrimaryContainer = Color(0xFFE0E0FF),
    secondary = Color(0xFFA9C7FF),
    onSecondary = Color(0xFF0A305F),
    secondaryContainer = Color(0xFF244873),
    onSecondaryContainer = Color(0xFFD6E3FF),
    error = Color(0xFFFFB3BA),
    onError = Color(0xFF680019),
    errorContainer = Color(0xFF8D1530),
    onErrorContainer = Color(0xFFFFDADD),
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE5E2EB),
    surface = Color(0xFF191B23),
    onSurface = Color(0xFFE5E2EB),
    surfaceVariant = Color(0xFF454752),
    onSurfaceVariant = Color(0xFFC6C5D0),
    outline = Color(0xFF90909E),
)

@Immutable
data class SageStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

private val LightStatusColors = SageStatusColors(
    success = SageGreen,
    onSuccess = Color.White,
    successContainer = Color(0xFFD8F3E4),
    onSuccessContainer = Color(0xFF062B1B),
    warning = SageAmber,
    onWarning = Color.White,
    warningContainer = Color(0xFFFFDDB3),
    onWarningContainer = Color(0xFF392100),
)

private val DarkStatusColors = SageStatusColors(
    success = Color(0xFF68D99D),
    onSuccess = Color(0xFF003921),
    successContainer = Color(0xFF155136),
    onSuccessContainer = Color(0xFFB6F1CD),
    warning = Color(0xFFFFB95C),
    onWarning = Color(0xFF4A2800),
    warningContainer = Color(0xFF603B08),
    onWarningContainer = Color(0xFFFFDDB3),
)

private val LocalSageStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.sageStatusColors: SageStatusColors
    @Composable get() = LocalSageStatusColors.current

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
fun SageSenseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
    CompositionLocalProvider(
        LocalSageStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
    ) {
        MaterialTheme(colorScheme = if (darkTheme) SageDarkColors else SageLightColors, typography = SageTypography) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, content = content)
        }
    }
}
