package az.saha.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.saha.app.R
import az.saha.app.data.repository.UserSession
import az.saha.app.ui.components.AtmosphereBackground
import az.saha.app.ui.navigation.SaheMenuButton
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.InkMuted
import az.saha.app.ui.theme.OliveDeep

@Composable
fun ProfileScreen(
    session: UserSession,
    onSignOut: () -> Unit,
    onOpenMenu: () -> Unit
) {
    AtmosphereBackground(
        imageRes = R.drawable.bg_soft_hills,
        scrim = Brush.verticalGradient(
            listOf(
                Color(0x99E8D9B8),
                Color(0xDDF4F0E6),
                Color(0xF2F4F0E6)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SaheMenuButton(onClick = onOpenMenu, tint = OliveDeep)
                Column {
                    Text(
                        "SAHƏ",
                        style = MaterialTheme.typography.titleMedium,
                        color = OliveDeep,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Profil",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Ink
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .padding(22.dp)
            ) {
                Text(
                    text = session.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = when {
                        session.isGuest -> "Qonaq rejimi · lokal yaddaş"
                        else -> session.email ?: "—"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkMuted
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (session.isGuest) {
                        "Ölçülər bu telefonda saxlanır. Firebase hesabı ilə buluda sinxron ola bilər."
                    } else {
                        "Ölçüləriniz hesabınıza bağlıdır və Firebase-də sinxron saxlanır. Reklam yoxdur."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OliveDeep)
            ) {
                Text("Çıxış", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
