package view.location

import location.LocationManager
import view.locationDialog.LocationPickerDialog
import view.locationDialog.PermissionDeniedDialog
import view.locationDialog.PermissionDeniedForeverDialog
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.mapbox.geojson.Point

private enum class PermissionState {
    ASKING,
    GRANTED,
    DENIED,
    DENIED_FOREVER
}

@SuppressLint("MissingPermission")
@Composable
fun LocationPermission(
    onDismiss: () -> Unit,
    onLocationSelected: (Point) -> Unit
) {

    val context = LocalContext.current
    val locationManager = remember {
        LocationManager(context)
    }

    var permissionState by remember { mutableStateOf(PermissionState.ASKING) }
    var userLocation by remember { mutableStateOf<Point?>(null) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var askPermission by remember { mutableStateOf(true) }

    if (askPermission) {
        RequestLocationPermission(
            onGranted = {
                askPermission = false
                permissionState = PermissionState.GRANTED
                Log.i("LocationPicker", "Permesso concesso, carico la posizione")

                isLoadingLocation = true
                locationManager.getCurrentLocation { lon, lat ->
                    userLocation = Point.fromLngLat(lon, lat)
                    isLoadingLocation = false
                    Log.i("LocationPicker", "User location: $lat, $lon")
                }
            },
            onDenied = {
                askPermission = false
                permissionState = PermissionState.DENIED
                Log.i("LocationPicker", "Permesso negato")
            },
            onDeniedForever = {
                askPermission = false
                permissionState = PermissionState.DENIED_FOREVER
                Log.i("LocationPicker", "Permesso negato per sempre")
            }
        )
    }

    when (permissionState) {
        PermissionState.ASKING -> {
            // Mostra un loading mentre aspetta la risposta
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7d0885))
                        Text("Waiting for permission...")
                    }
                }
            }
        }

        PermissionState.DENIED -> {
            PermissionDeniedDialog(onDismiss = onDismiss)
        }

        PermissionState.DENIED_FOREVER -> {
            PermissionDeniedForeverDialog(onDismiss = onDismiss)
        }

        PermissionState.GRANTED -> {
            if (isLoadingLocation) {
                Dialog(
                    onDismissRequest = { },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF7d0885))
                            Text("Loading position...")
                        }
                    }
                }
            } else {
                LocationPickerDialog(
                    startLocation = userLocation ?: Point.fromLngLat(9.2319522, 45.4759249),
                    onDismiss = onDismiss,
                    onLocationSelected = onLocationSelected
                )
            }
        }
    }
}