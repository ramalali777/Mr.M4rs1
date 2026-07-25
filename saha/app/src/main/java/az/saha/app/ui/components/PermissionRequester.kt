package az.saha.app.ui.components

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionRequester(permissions: List<String>) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* map handles disabled location gracefully */ }

    LaunchedEffect(permissions) {
        val missing = permissions.any { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }
        if (missing && activity != null) {
            launcher.launch(permissions.toTypedArray())
        }
    }
}
