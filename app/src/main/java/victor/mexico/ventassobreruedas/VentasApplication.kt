package victor.mexico.ventassobreruedas

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import victor.mexico.ventassobreruedas.data.auth.AuthRepository
import victor.mexico.ventassobreruedas.data.repository.SalesRepository

/** Contenedor sencillo de dependencias (inyección manual). */
class VentasApplication : Application() {

    val authRepository: AuthRepository by lazy {
        AuthRepository(FirebaseAuth.getInstance(), getString(R.string.default_web_client_id))
    }

    private var cachedSalesRepository: SalesRepository? = null

    /** Ventas del usuario con sesión iniciada; solo se usa en pantallas posteriores al login. */
    val salesRepository: SalesRepository
        get() {
            val uid = checkNotNull(authRepository.currentUser?.uid) { "No hay sesión iniciada" }
            return cachedSalesRepository?.takeIf { it.uid == uid }
                ?: SalesRepository(FirebaseFirestore.getInstance(), uid).also { cachedSalesRepository = it }
        }
}

private val Context.app: VentasApplication get() = applicationContext as VentasApplication

@Composable
fun salesRepository(): SalesRepository = LocalContext.current.app.salesRepository

@Composable
fun authRepository(): AuthRepository = LocalContext.current.app.authRepository
