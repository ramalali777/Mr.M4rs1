package az.saha.app.data.repository

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val googleClient: GoogleSignInClient
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun googleSignInIntent(): Intent = googleClient.signInIntent

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.signInWithEmailAndPassword(email.trim(), password).await().user
                ?: error("İstifadəçi tapılmadı")
        }

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.createUserWithEmailAndPassword(email.trim(), password).await().user
                ?: error("Hesab yaradılmadı")
        }

    suspend fun signInWithGoogleIntent(data: Intent?): Result<FirebaseUser> = runCatching {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).await().user
            ?: error("Google girişi uğursuz oldu")
    }

    suspend fun signOut() {
        runCatching { googleClient.signOut().await() }
        auth.signOut()
    }
}
