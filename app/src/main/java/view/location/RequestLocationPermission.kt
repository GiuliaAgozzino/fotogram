package view.location


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onDeniedForever: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onGranted()
        } else {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission
            )

            if (showRationale) {
                onDenied() // Don't allow
            } else {
                onDeniedForever() // Don't ask again
            }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission()) {
            onGranted()
        } else {
            launcher.launch(permission)
        }
    }
}