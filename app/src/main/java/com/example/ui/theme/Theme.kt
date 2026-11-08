package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ExecutiveDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkNavyBase,
    primaryContainer = GoldDark,
    onPrimaryContainer = TextWhite,
    secondary = EmeraldProfit,
    onSecondary = DarkNavyBase,
    secondaryContainer = DarkNavyCardElevated,
    onSecondaryContainer = EmeraldLight,
    tertiary = ElectricBlue,
    onTertiary = DarkNavyBase,
    background = DarkNavyBase,
    onBackground = TextWhite,
    surface = DarkNavySurface,
    onSurface = TextWhite,
    surfaceVariant = DarkNavyCard,
    onSurfaceVariant = TextLight,
    outline = SlateBorder,
    error = RoseLoss,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExecutiveDarkColorScheme,
        typography = Typography,
        content = content
    )
}
