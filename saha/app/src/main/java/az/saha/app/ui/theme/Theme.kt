package az.saha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SahaColors = lightColorScheme(
    primary = Olive,
    onPrimary = Color.White,
    primaryContainer = MistDark,
    onPrimaryContainer = Ink,
    secondary = Clay,
    onSecondary = Ink,
    background = Mist,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = MistDark,
    onSurfaceVariant = InkMuted,
    error = Danger,
    onError = Color.White
)

@Composable
fun SahaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SahaColors,
        typography = SahaTypography,
        content = content
    )
}
