package az.saha.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import az.saha.app.R
import az.saha.app.ui.theme.Clay
import az.saha.app.ui.theme.Ink
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_login_field),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x33000000),
                            Color(0x11000000),
                            Color(0x661A2420)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { -48 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SAHƏ",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = OliveDeep,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Torpağı ölç, hesabında saxla",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Ink.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 100 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    // Frosted glass look
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xE6F4F0E6))
                            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                    )
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.isRegisterMode) "Hesab yarat" else "Daxil ol",
                            style = MaterialTheme.typography.headlineMedium,
                            color = OliveDeep,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::onEmail,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("E-poçt") },
                            leadingIcon = { Icon(Icons.Outlined.Email, null, tint = Olive) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors()
                        )

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::onPassword,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Şifrə") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Olive) },
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePassword) {
                                    Icon(
                                        if (state.passwordVisible) Icons.Outlined.VisibilityOff
                                        else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = Olive
                                    )
                                }
                            },
                            visualTransformation = if (state.passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors()
                        )

                        if (state.error != null) {
                            Text(
                                text = state.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = viewModel::submit,
                            enabled = !state.loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OliveDeep)
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    if (state.isRegisterMode) "Qeydiyyat" else "Daxil ol",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        TextButton(onClick = viewModel::toggleMode) {
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

                        Button(
                            onClick = { googleLauncher.launch(viewModel.googleIntent()) },
                            enabled = !state.loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Ink
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_google),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Google ilə davam et", fontWeight = FontWeight.Medium)
                        }

                        TextButton(
                            onClick = viewModel::continueAsGuest,
                            enabled = !state.loading
                        ) {
                            Text("Qonaq kimi davam et", color = OliveDeep.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun RowDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Clay.copy(alpha = 0.55f))
        Text(
            text = "  və ya  ",
            color = Ink.copy(alpha = 0.55f),
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Clay.copy(alpha = 0.55f))
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.85f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.72f),
    focusedBorderColor = Olive,
    unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = Olive,
    cursorColor = Olive,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink
)
