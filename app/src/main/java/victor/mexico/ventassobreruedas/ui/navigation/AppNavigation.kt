package victor.mexico.ventassobreruedas.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import victor.mexico.ventassobreruedas.authRepository
import victor.mexico.ventassobreruedas.ui.days.SalesDaysRoute
import victor.mexico.ventassobreruedas.ui.home.HomeScreen
import victor.mexico.ventassobreruedas.ui.login.LoginRoute
import victor.mexico.ventassobreruedas.ui.sales.SalesRoute
import victor.mexico.ventassobreruedas.ui.summary.SummaryRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data object HomeKey : NavKey

@Serializable
data object SalesKey : NavKey

@Serializable
data object SalesDaysKey : NavKey

/** La fecha se guarda como epoch day porque LocalDate no es serializable directamente. */
@Serializable
data class SummaryKey(val epochDay: Long) : NavKey {
    constructor(date: LocalDate) : this(date.toEpochDay())

    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
}

/** Sin sesión se muestra el login; al iniciar sesión se pasa a la navegación normal. */
@Composable
fun AppNavigation() {
    val authRepository = authRepository()
    val userFlow = remember(authRepository) { authRepository.observeUser() }
    val user by userFlow.collectAsStateWithLifecycle(initialValue = authRepository.currentUser)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Crossfade(targetState = user, label = "auth") { currentUser ->
        if (currentUser == null) {
            LoginRoute()
        } else {
            SignedInNavigation(
                // Solo el primer nombre: "Victor Manuel López" -> "Victor".
                userName = currentUser.displayName?.substringBefore(' '),
                onSignOut = { scope.launch { authRepository.signOut(context) } },
            )
        }
    }
}

@Composable
private fun SignedInNavigation(userName: String?, onSignOut: () -> Unit) {
    val backStack = rememberNavBackStack(HomeKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    userName = userName,
                    onRegisterSales = { backStack.add(SalesKey) },
                    onViewSales = { backStack.add(SalesDaysKey) },
                    onSignOut = onSignOut,
                )
            }
            entry<SalesKey> {
                SalesRoute(
                    onBack = { backStack.removeLastOrNull() },
                    // Al finalizar se reemplaza el registro por el resumen: "atrás" vuelve a Home.
                    onFinishDay = {
                        backStack.removeLastOrNull()
                        backStack.add(SummaryKey(LocalDate.now()))
                    },
                )
            }
            entry<SalesDaysKey> {
                SalesDaysRoute(
                    onBack = { backStack.removeLastOrNull() },
                    onDayClick = { date -> backStack.add(SummaryKey(date)) },
                )
            }
            entry<SummaryKey> { key ->
                SummaryRoute(date = key.date, onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
