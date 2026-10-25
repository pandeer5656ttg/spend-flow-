package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldLight,
  onPrimary = Color(0xFF022C22),
  primaryContainer = EmeraldDark,
  onPrimaryContainer = Color(0xFFD1FAE5),
  secondary = InfoBlue,
  onSecondary = Color(0xFF082F49),
  secondaryContainer = Color(0xFF0369A1),
  onSecondaryContainer = Color(0xFFE0F2FE),
  tertiary = AccentGold,
  onTertiary = Color(0xFF451A03),
  tertiaryContainer = Color(0xFF78350F),
  onTertiaryContainer = Color(0xFFFEF3C7),
  background = SlateBackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SlateSurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SlateSurfaceCard,
  onSurfaceVariant = TextSecondaryDark,
  outline = SlateBorder,
  error = ExpenseCoral,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldDark,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD1FAE5),
  onPrimaryContainer = Color(0xFF064E3B),
  secondary = Color(0xFF0284C7),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE0F2FE),
  onSecondaryContainer = Color(0xFF0C4A6E),
  tertiary = Color(0xFFB45309),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFFEF3C7),
  onTertiaryContainer = Color(0xFF78350F),
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color.White,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = Color(0xFFDC2626),
  onError = Color.White
)

@Composable
fun SpendFlowTheme(
  darkTheme: Boolean = true, // Default to sleek modern dark fintech theme
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.surface.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
