package br.com.watchusee.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PremiumGold,
    secondary = AccentBlue,
    tertiary = TextWhite,
    background = DarkNavy,
    surface = SurfaceGrey,
    surfaceVariant = JetBlack,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextGrey,
    outline = GraySubtle
)

@Composable
fun WatchuSeeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
