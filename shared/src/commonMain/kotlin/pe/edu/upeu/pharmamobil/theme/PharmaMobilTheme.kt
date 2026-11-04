package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Marca de la sesión 13: rojo predominante y superficies neutras legibles. */
private val LightColors = lightColorScheme(
    primary = Color(0xFFC8102E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8B1023),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF8B1023),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6EB),
    onSecondaryContainer = Color(0xFF8B1023),
    tertiary = Color(0xFFE53935),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE6EB),
    onTertiaryContainer = Color(0xFF8B1023),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF172033),
    surface = Color(0xFFF7F8FA),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFEDF0F5),
    onSurfaceVariant = Color(0xFF596174),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    outline = Color(0xFF717888),
    outlineVariant = Color(0xFFD4D9E2)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB2BD),
    onPrimary = Color(0xFF560019),
    primaryContainer = Color(0xFF8B1023),
    onPrimaryContainer = Color(0xFF8B1023),
    secondary = Color(0xFFF0BCC5),
    onSecondary = Color(0xFF40101B),
    secondaryContainer = Color(0xFF60232F),
    onSecondaryContainer = Color(0xFFFFE6EB),
    tertiary = Color(0xFFFFB2AA),
    onTertiary = Color(0xFF5C1713),
    tertiaryContainer = Color(0xFF7C2C27),
    onTertiaryContainer = Color(0xFFFFE6EB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF172033),
    onBackground = Color(0xFFE5E9F0),
    surface = Color(0xFF172033),
    onSurface = Color(0xFFE5E9F0),
    surfaceVariant = Color(0xFF596174),
    onSurfaceVariant = Color(0xFFD4D9E2),
    surfaceContainerLowest = Color(0xFF10151F),
    surfaceContainerLow = Color(0xFF172033),
    surfaceContainer = Color(0xFF1D2635),
    surfaceContainerHigh = Color(0xFF273043),
    surfaceContainerHighest = Color(0xFF323C50),
    outline = Color(0xFFA8B0BF),
    outlineVariant = Color(0xFF596174)
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
