package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SafeBlueLight,
    onPrimary = SafeDarkBg,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = SafeTealLight,
    onSecondary = SafeDarkBg,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = SafeOrangeLight,
    onTertiary = Color(0xFF7C2D12),
    tertiaryContainer = Color(0xFF7C2D12),
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = SafeDarkBg,
    surface = SafeDarkSurface,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = SafeCoral,
    onError = Color.White,
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SafeBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = SafeTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = SafeOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF7C2D12),
    background = SafeLightBg,
    surface = SafeLightSurface,
    onBackground = SafeTextDark,
    onSurface = SafeTextDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SafeTextMuted,
    outline = SafeCardBorder,
    outlineVariant = Color(0xFFF1F5F9),
    error = SafeCoral,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

/**
 * Smoothly interpolates and cross-fades all ColorScheme tokens during theme transitions.
 */
@Composable
fun animateColorSchemeAsState(
  targetColorScheme: ColorScheme,
  animationSpec: AnimationSpec<Color> = tween(durationMillis = 400, easing = FastOutSlowInEasing)
): ColorScheme {
  val primary by animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary")
  val onPrimary by animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary")
  val primaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer")
  val onPrimaryContainer by animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer")
  val inversePrimary by animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary")
  val secondary by animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary")
  val onSecondary by animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary")
  val secondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer")
  val onSecondaryContainer by animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer")
  val tertiary by animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary")
  val onTertiary by animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary")
  val tertiaryContainer by animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer")
  val onTertiaryContainer by animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer")
  val background by animateColorAsState(targetColorScheme.background, animationSpec, label = "background")
  val onBackground by animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground")
  val surface by animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface")
  val onSurface by animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface")
  val surfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant")
  val onSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant")
  val surfaceTint by animateColorAsState(targetColorScheme.surfaceTint, animationSpec, label = "surfaceTint")
  val inverseSurface by animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface")
  val inverseOnSurface by animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface")
  val error by animateColorAsState(targetColorScheme.error, animationSpec, label = "error")
  val onError by animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError")
  val errorContainer by animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer")
  val onErrorContainer by animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer")
  val outline by animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline")
  val outlineVariant by animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant")
  val scrim by animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim")

  return targetColorScheme.copy(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    inversePrimary = inversePrimary,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    surfaceTint = surfaceTint,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = scrim
  )
}

@Composable
fun SafeTubeTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val darkTheme = themeMode.isDark(isSystemDark)

  val targetColorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  // Smooth cross-fade animation across all UI components and surfaces
  val animatedColorScheme = animateColorSchemeAsState(
    targetColorScheme = targetColorScheme,
    animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
  )

  CompositionLocalProvider(
    LocalThemeMode provides themeMode,
    LocalIsDarkTheme provides darkTheme
  ) {
    MaterialTheme(colorScheme = animatedColorScheme, typography = Typography) {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = animatedColorScheme.background
      ) {
        content()
      }
    }
  }
}

@Composable
fun SafeTubeTheme(
  darkTheme: Boolean,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT
  SafeTubeTheme(themeMode = themeMode, dynamicColor = dynamicColor, content = content)
}
