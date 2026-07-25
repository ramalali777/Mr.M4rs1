package az.saha.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.saha.app.data.repository.UserSession
import az.saha.app.ui.theme.Ink
import az.saha.app.ui.theme.InkMuted
import az.saha.app.ui.theme.Mist
import az.saha.app.ui.theme.Olive
import az.saha.app.ui.theme.OliveDeep

@Composable
fun ProfileScreen(
    session: UserSession,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8D9B8), Mist)))
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(48.dp))
        Text("SAHƏ", style = MaterialTheme.typography.titleMedium, color = OliveDeep)
        Text("Profil", style = MaterialTheme.typography.headlineLarge, color = Ink)
        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text(
                text = session.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
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
                    "Ölçülər bu telefonda saxlanır. Firebase qoşulanda hesabla buluda sinxron olacaq."
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
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Olive)
        ) {
            Text("Çıxış")
        }
        Spacer(Modifier.height(24.dp))
    }
}
