package az.saha.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.Mist
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleResult(result.data)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFB7C9A8), Color(0xFFE8D9B8), Mist)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))
            AnimatedVisibility(visible = appeared, enter = fadeIn() + slideInVertically { -40 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SAHA",
                        style = MaterialTheme.typography.displayLarge,
                        color = OliveDeep,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Torpağı ölç, hesabında saxla",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn() + slideInVertically { 80 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(28.dp))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (state.isRegisterMode) "Hesab yarat" else "Daxil ol",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmail,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("E-poçt") },
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = fieldColors()
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPassword,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Şifrə") },
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePassword) {
                                Icon(
                                    if (state.passwordVisible) Icons.Outlined.VisibilityOff
                                    else Icons.Outlined.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (state.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        colors = fieldColors()
                    )

                    if (state.error != null) {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = viewModel::submit,
                        enabled = !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Olive)
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.height(22.dp)
                            )
                        } else {
                            Text(if (state.isRegisterMode) "Qeydiyyat" else "Daxil ol")
                        }
                    }

                    TextButton(
                        onClick = viewModel::toggleMode,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (state.isRegisterMode) {
                                "Artıq hesabın var? Daxil ol"
                            } else {
                                "Hesab yarat"
                            },
                            color = OliveDeep
                        )
                    }

                    RowDivider()

                    OutlinedButton(
                        onClick = { googleLauncher.launch(viewModel.googleIntent()) },
                        enabled = !state.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Google ilə davam et", color = Ink)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun RowDivider() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        HorizontalDivider(color = Clay.copy(alpha = 0.45f))
        Text(
            text = "  və ya  ",
            modifier = Modifier.background(Color.White.copy(alpha = 0.92f)),
            color = Ink.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Olive,
    unfocusedBorderColor = Clay.copy(alpha = 0.5f),
    focusedLabelColor = Olive,
    cursorColor = Olive
)
