package victor.mexico.ventassobreruedas.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.authRepository
import victor.mexico.ventassobreruedas.ui.components.MaxFormWidth
import victor.mexico.ventassobreruedas.ui.theme.MyApplicationTheme
import victor.mexico.ventassobreruedas.ui.theme.SeaDeep
import victor.mexico.ventassobreruedas.ui.theme.SeaShallow

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.factory(authRepository())),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LoginScreen(uiState = uiState, onSignIn = { viewModel.signIn(context) })
}

/** Borde inferior en forma de ola para el encabezado (pantalla vertical). */
private val WaveBottomShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w, 0f)
    lineTo(w, h * 0.82f)
    cubicTo(w * 0.72f, h * 1.04f, w * 0.38f, h * 0.70f, 0f, h * 0.92f)
    close()
}

/** Borde derecho en forma de ola para el encabezado (pantalla horizontal). */
private val WaveEndShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, 0f)
    lineTo(w * 0.88f, 0f)
    cubicTo(w * 1.04f, h * 0.30f, w * 0.72f, h * 0.62f, w * 0.94f, h)
    lineTo(0f, h)
    close()
}

/**
 * En vertical: encabezado arriba y tarjeta abajo, con scroll si no cabe.
 * En horizontal (teléfono acostado o tablet): encabezado a la izquierda y tarjeta a la derecha.
 */
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (maxWidth > maxHeight && maxWidth >= 600.dp) {
            Row(Modifier.fillMaxSize()) {
                LoginHeader(
                    shape = WaveEndShape,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End + WindowInsetsSides.Vertical))
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WelcomeCard(uiState, onSignIn)
                    Spacer(Modifier.height(24.dp))
                    SecureFooter()
                }
            }
        } else {
            val headerHeight = (maxHeight * 0.45f).coerceIn(240.dp, 440.dp)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoginHeader(
                        shape = WaveBottomShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight),
                    )
                    // La tarjeta sube un poco para encimarse con la ola.
                    WelcomeCard(
                        uiState = uiState,
                        onSignIn = onSignIn,
                        modifier = Modifier
                            .offset(y = (-24).dp)
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                            .padding(horizontal = 24.dp),
                    )
                }
                SecureFooter(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun LoginHeader(shape: Shape, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(SeaShallow, SeaDeep))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Start))
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WelcomeCard(
    uiState: LoginUiState,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .widthIn(max = MaxFormWidth)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.welcome),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.sign_in_explanation),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            GoogleSignInButton(isLoading = uiState.isLoading, onClick = onSignIn)
            uiState.errorRes?.let {
                Text(
                    stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SecureFooter(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.sign_in_footer),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GoogleSignInButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        // Altura mínima (no fija) para que el texto no se corte con letra grande.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(R.string.sign_in_google),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Teléfono", showBackground = true, device = Devices.PIXEL_7)
@Preview(name = "Teléfono horizontal", showBackground = true, device = "spec:width=891dp,height=411dp")
@Preview(name = "Tablet", showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
private fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreen(uiState = LoginUiState(), onSignIn = {})
    }
}
