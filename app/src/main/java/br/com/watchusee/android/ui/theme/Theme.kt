package br.com.watchusee.android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme

private val DarkColorScheme = darkColorScheme(
    primary = CinemaAccent,
    secondary = TextWhite,
    tertiary = TextWhite,
    background = CinemaCanvas,
    surface = CinemaPanel,
    surfaceVariant = CinemaPanelRaised,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = CinemaText,
    onSurface = CinemaText,
    onSurfaceVariant = CinemaMuted,
    outline = GraySubtle
)

private val LightColorScheme = lightColorScheme(
    primary = CinemaAccent,
    secondary = TextWhite,
    tertiary = TextWhite,
    background = CinemaCanvas,
    surface = CinemaPanel,
    surfaceVariant = CinemaPanelRaised,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = CinemaText,
    onSurface = CinemaText,
    onSurfaceVariant = CinemaMuted,
    outline = GraySubtle
)

@Composable
fun WatchuSeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
