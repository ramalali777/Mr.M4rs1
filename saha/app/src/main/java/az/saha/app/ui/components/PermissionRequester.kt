package az.saha.app.ui.components

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberPermissionGranted(
    permissions: List<String>,
    requestIfMissing: Boolean = true
): Boolean {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    fun checkAll(): Boolean = permissions.all { perm ->
        ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
    }

    var granted by remember { mutableStateOf(checkAll()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.any { it } || checkAll()
    }

    LaunchedEffect(permissions) {
        granted = checkAll()
        if (requestIfMissing && !granted && activity != null) {
            launcher.launch(permissions.toTypedArray())
        }
    }

    return granted
}

/** Backward-compatible fire-and-forget requester */
@Composable
fun PermissionRequester(permissions: List<String>) {
    rememberPermissionGranted(permissions = permissions, requestIfMissing = true)
}
