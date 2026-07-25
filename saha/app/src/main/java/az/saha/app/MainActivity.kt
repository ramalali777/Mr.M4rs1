package az.saha.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import az.saha.app.di.AppContainer
import az.saha.app.ui.auth.AuthViewModel
import az.saha.app.ui.auth.LoginScreen
import az.saha.app.ui.navigation.SahaMainNav
import az.saha.app.ui.theme.Mist
import az.saha.app.ui.theme.SahaTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as SahaApp).container

        val authState = container.authRepository.authState.stateIn(
            lifecycleScope,
            SharingStarted.WhileSubscribed(5_000),
            container.authRepository.currentUser
        )

        setContent {
            SahaTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Mist) {
                    val user by authState.collectAsStateWithLifecycle()
                    val scope = rememberCoroutineScope()
                    if (user == null) {
                        AuthGate(container)
                    } else {
                        SahaMainNav(
                            container = container,
                            user = user!!,
                            onSignOut = {
                                scope.launch { container.authRepository.signOut() }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthGate(container: AppContainer) {
    val vm: AuthViewModel = viewModel(factory = AuthViewModel.factory(container.authRepository))
    LoginScreen(vm)
}
