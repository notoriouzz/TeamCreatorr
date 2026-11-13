package nl.tartaruzz.teamcreatorr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Grass,
    onPrimary = Color(0xFF0B2B12),
    primaryContainer = PitchGreenDark,
    onPrimaryContainer = Grass,
    secondary = GoldLight,
    onSecondary = Color(0xFF3E2E00),
    tertiary = TeamAColor,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB8C4BD),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    outline = Color(0xFF5C6B63)
)

private val LightColorScheme = lightColorScheme(
    primary = PitchGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBCE8CF),
    onPrimaryContainer = PitchGreenDark,
    secondary = Color(0xFF8A6D00),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = TeamAColor,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF45544B),
    error = Color(0xFFC62828),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF72857A)
)

@Composable
fun TeamCreatorrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
