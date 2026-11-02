package victor.mexico.ventassobreruedas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

private val LightColorScheme = lightColorScheme(
    primary = TurquoisePrimaryLight,
    onPrimary = TurquoiseOnPrimaryLight,
    primaryContainer = TurquoisePrimaryContainerLight,
    onPrimaryContainer = TurquoiseOnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = OceanTertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = OceanTertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = TurquoisePrimaryDark,
    onPrimary = TurquoiseOnPrimaryDark,
    primaryContainer = TurquoisePrimaryContainerDark,
    onPrimaryContainer = TurquoiseOnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = OceanTertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = OceanTertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

/**
 * Tema turquesa de la app. No se usan los colores dinámicos de Android 12+ (tomados del
 * fondo de pantalla) para que la app siempre conserve su paleta.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Tablet = el lado más corto de la pantalla mide 600dp o más (también en horizontal).
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = if (isTablet) TabletTypography else Typography,
        content = content,
    )
}
