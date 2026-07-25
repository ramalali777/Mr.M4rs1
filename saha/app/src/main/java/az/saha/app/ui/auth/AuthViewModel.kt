package az.saha.app.ui.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.saha.app.data.repository.AuthRepository
import az.saha.app.data.repository.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val passwordVisible: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun onEmail(v: String) = _ui.update { it.copy(email = v, error = null) }
    fun onPassword(v: String) = _ui.update { it.copy(password = v, error = null) }
    fun toggleMode() = _ui.update {
        it.copy(isRegisterMode = !it.isRegisterMode, error = null)
    }
    fun togglePassword() = _ui.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun continueAsGuest() {
        sessionStore.enterGuest()
    }

    fun submit() {
        val state = _ui.value
        if (state.email.isBlank() || state.password.length < 6) {
            _ui.update { it.copy(error = "E-poçt və ən azı 6 simvollu şifrə daxil edin") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            val result = if (state.isRegisterMode) {
                authRepository.registerWithEmail(state.email, state.password)
            } else {
                authRepository.signInWithEmail(state.email, state.password)
            }
            result.onSuccess { user ->
                sessionStore.enterFirebase(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email
                )
            }
            _ui.update {
                it.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: if (result.isFailure) {
                            "Firebase hələ qoşulmayıb. Qonaq kimi davam edə bilərsiniz."
                        } else null
                )
            }
        }
    }

    fun googleIntent(): Intent = authRepository.googleSignInIntent()

    fun handleGoogleResult(data: Intent?) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            val result = authRepository.signInWithGoogleIntent(data)
            result.onSuccess { user ->
                sessionStore.enterFirebase(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email
                )
            }
            _ui.update {
                it.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: if (result.isFailure) {
                            "Google giriş işləmir. Qonaq kimi davam edin."
                        } else null
                )
            }
        }
    }

    companion object {
        fun factory(repo: AuthRepository, sessionStore: SessionStore) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repo, sessionStore) as T
                }
            }
    }
}
