package victor.mexico.ventassobreruedas.ui.login

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.auth.AuthRepository

data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Al iniciar sesión, la navegación cambia sola a Home (ver AppNavigation). */
    fun signIn(activityContext: Context) {
        if (_uiState.value.isLoading) return
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            val error = try {
                authRepository.signInWithGoogle(activityContext)
                null
            } catch (e: GetCredentialCancellationException) {
                null
            } catch (e: NoCredentialException) {
                R.string.error_no_google_account
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al iniciar sesión", e)
                R.string.error_sign_in
            }
            _uiState.value = LoginUiState(errorRes = error)
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository) = viewModelFactory {
            initializer { LoginViewModel(authRepository) }
        }
    }
}
