package victor.mexico.ventassobreruedas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified

/** Escala tipográfica de Material 3 para teléfonos. Los tamaños en sp respetan el tamaño de letra del sistema. */
val Typography = Typography()

/**
 * En tablets la pantalla se ve desde más lejos, así que el texto se agranda un poco
 * para que se lea igual de cómodo que en un teléfono.
 */
private const val TabletTextScale = 1.15f

val TabletTypography = Typography.scaled(TabletTextScale)

private fun Typography.scaled(factor: Float) = Typography(
    displayLarge = displayLarge.scaled(factor),
    displayMedium = displayMedium.scaled(factor),
    displaySmall = displaySmall.scaled(factor),
    headlineLarge = headlineLarge.scaled(factor),
    headlineMedium = headlineMedium.scaled(factor),
    headlineSmall = headlineSmall.scaled(factor),
    titleLarge = titleLarge.scaled(factor),
    titleMedium = titleMedium.scaled(factor),
    titleSmall = titleSmall.scaled(factor),
    bodyLarge = bodyLarge.scaled(factor),
    bodyMedium = bodyMedium.scaled(factor),
    bodySmall = bodySmall.scaled(factor),
    labelLarge = labelLarge.scaled(factor),
    labelMedium = labelMedium.scaled(factor),
    labelSmall = labelSmall.scaled(factor),
)

private fun TextStyle.scaled(factor: Float) = copy(
    fontSize = fontSize.times(factor),
    lineHeight = lineHeight.times(factor),
)

private fun TextUnit.times(factor: Float): TextUnit = if (isSpecified) this * factor else this
