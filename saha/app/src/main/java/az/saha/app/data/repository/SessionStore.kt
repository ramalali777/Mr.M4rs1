package az.saha.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class UserSession(
    val userId: String,
    val displayName: String,
    val email: String?,
    val isGuest: Boolean
)

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("saha_session", Context.MODE_PRIVATE)
    private val _session = MutableStateFlow(read())
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private fun read(): UserSession? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        return UserSession(
            userId = id,
            displayName = prefs.getString(KEY_NAME, "İstifadəçi") ?: "İstifadəçi",
            email = prefs.getString(KEY_EMAIL, null),
            isGuest = prefs.getBoolean(KEY_GUEST, true)
        )
    }

    fun enterGuest() {
        val id = prefs.getString(KEY_ID, null)?.takeIf {
            prefs.getBoolean(KEY_GUEST, false)
        } ?: "guest-${UUID.randomUUID()}"
        save(
            UserSession(
                userId = id,
                displayName = "Qonaq",
                email = null,
                isGuest = true
            )
        )
    }

    fun enterFirebase(userId: String, displayName: String?, email: String?) {
        save(
            UserSession(
                userId = userId,
                displayName = displayName?.takeIf { it.isNotBlank() } ?: "İstifadəçi",
                email = email,
                isGuest = false
            )
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private fun save(session: UserSession) {
        prefs.edit()
            .putString(KEY_ID, session.userId)
            .putString(KEY_NAME, session.displayName)
            .putString(KEY_EMAIL, session.email)
            .putBoolean(KEY_GUEST, session.isGuest)
            .apply()
        _session.value = session
    }

    companion object {
        private const val KEY_ID = "user_id"
        private const val KEY_NAME = "display_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_GUEST = "is_guest"
    }
}
