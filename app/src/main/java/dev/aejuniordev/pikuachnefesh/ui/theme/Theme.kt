package dev.aejuniordev.pikuachnefesh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LavenderDeep,
    onPrimary = Color.White,
    primaryContainer = Lavender,
    onPrimaryContainer = Ink,
    secondary = MintDeep,
    onSecondary = Color.White,
    secondaryContainer = Mint,
    onSecondaryContainer = Ink,
    tertiary = PeachDeep,
    onTertiary = Color.White,
    tertiaryContainer = Peach,
    onTertiaryContainer = Ink,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCF7F5),
    surfaceContainer = Color(0xFFF8F3F4),
    surfaceContainerHigh = Mist,
    surfaceContainerHighest = Color(0xFFEBE5EF),
    outline = Color(0xFFB8B0C6),
    outlineVariant = Color(0xFFDCD5E4),
)

private val DarkColorScheme = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF2E2648),
    primaryContainer = LavenderNight,
    onPrimaryContainer = Moon,
    secondary = Mint,
    onSecondary = Color(0xFF1D3A2E),
    secondaryContainer = MintNight,
    onSecondaryContainer = Moon,
    tertiary = Peach,
    onTertiary = Color(0xFF4A2A18),
    tertiaryContainer = PeachNight,
    onTertiaryContainer = Moon,
    background = Night,
    onBackground = Moon,
    surface = Night,
    onSurface = Moon,
    surfaceVariant = NightHigh,
    onSurfaceVariant = Color(0xFFC9C2D4),
    surfaceContainerHigh = NightHigh,
    outline = Color(0xFF8A8396),
)

@Composable
fun PikuachNefeshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Sem "dynamic color": no Android 12+ ele trocaria a paleta pastel pelas cores do papel de parede.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
