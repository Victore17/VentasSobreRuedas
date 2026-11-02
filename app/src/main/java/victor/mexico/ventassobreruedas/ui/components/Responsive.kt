package victor.mexico.ventassobreruedas.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ancho máximo del contenido. En tablets y en horizontal el contenido se centra con este
 * ancho en lugar de estirarse de orilla a orilla (botones y listas enormes son difíciles de leer).
 */
val MaxContentWidth = 640.dp

/** Ancho máximo para formularios y tarjetas pequeñas (login, botones de Inicio). */
val MaxFormWidth = 480.dp

/**
 * Da a [content] el padding para una lista: el de la Scaffold ([innerPadding]) más un margen
 * lateral que centra el contenido en [maxWidth] cuando la pantalla es más ancha. Así la lista
 * sigue ocupando toda la pantalla (se puede desplazar desde cualquier parte) pero sus filas
 * quedan centradas.
 */
@Composable
fun CenteredContentPadding(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    maxWidth: Dp = MaxContentWidth,
    extraBottom: Dp = 0.dp,
    content: @Composable (PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    BoxWithConstraints(modifier) {
        val start = innerPadding.calculateStartPadding(layoutDirection)
        val end = innerPadding.calculateEndPadding(layoutDirection)
        val side = ((this.maxWidth - start - end - maxWidth) / 2).coerceAtLeast(0.dp)
        content(
            PaddingValues(
                start = start + side,
                top = innerPadding.calculateTopPadding(),
                end = end + side,
                bottom = innerPadding.calculateBottomPadding() + extraBottom,
            ),
        )
    }
}
