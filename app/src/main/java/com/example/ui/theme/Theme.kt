package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = FarmGreenLight,
    onPrimary = OnFarmGreen,
    primaryContainer = FarmGreenPrimary,
    secondary = FloodBlueLight,
    tertiary = HarvestGold,
    background = TextPrimary,
    surface = TextPrimary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = OnFarmGreen,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = FarmGreenPrimary,
    secondary = FloodBlue,
    secondaryContainer = FloodBlueContainer,
    tertiary = HarvestGold,
    tertiaryContainer = HarvestGoldContainer,
    background = SurfaceLight,
    surface = SurfaceCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = OutlineBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use consistent, brand-aligned agricultural high-contrast colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

