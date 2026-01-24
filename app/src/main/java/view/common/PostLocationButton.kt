package view.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import utils.LocationManager
import model.Post
import view.location.RequestLocationPermission
import view.locationDialog.LocationViewerDialog

@SuppressLint("MissingPermission")
@Composable
fun PostLocationButton(post: Post) {
    // Se non c'è location, non mostrare nulla
    if (post.location?.latitude == null || post.location.longitude == null) {
        return
    }

    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }

    // Stati per la posizione utente
    var userLocation by remember { mutableStateOf<Point?>(null) }
    var isNear by remember { mutableStateOf<Boolean?>(null) }
    var distanceKm by remember { mutableStateOf<Double?>(null) }

    // Stati per permessi e dialog
    var showMapDialog by remember { mutableStateOf(false) }
    var requestPermission by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    // UI: bottone per aprire la mappa
    Row(
        modifier = Modifier
            .clickable {
                showMapDialog = true
                requestPermission = true
            }
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Posizione",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Mappa",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }

    // Richiedi permesso solo quando l'utente clicca
    if (requestPermission) {
        RequestLocationPermission(
            onGranted = {
                Log.d("PostLocationButton", "Permesso concesso")
                permissionGranted = true
                requestPermission = false
            },
            onDenied = {
                Log.d("PostLocationButton", "Permesso negato")
                permissionGranted = false
                requestPermission = false
            },
            onDeniedForever = {
                Log.d("PostLocationButton", "Permesso negato per sempre")
                permissionGranted = false
                requestPermission = false
            }
        )
    }

    // Quando otteniamo il permesso, carica la posizione utente
    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) return@LaunchedEffect

        locationManager.getCurrentLocation { lon, lat ->
            userLocation = Point.fromLngLat(lon, lat)
            Log.d("PostLocationButton", "User location: $userLocation")

            // Calcola distanza dal post
            val distance = locationManager.distanceInKm(
                lat, lon,
                post.location.latitude, post.location.longitude
            )
            distanceKm = distance
            isNear = distance <= 5.0

            Log.d("PostLocationButton", "Distanza: $distance km, isNear: $isNear")
        }
    }

    // Dialog con la mappa
    if (showMapDialog) {
        LocationViewerDialog(
            postLocation = Point.fromLngLat(
                post.location.longitude,
                post.location.latitude
            ),
            userLocation = userLocation,
            isNear = isNear,
            distanceKm = distanceKm,
            onDismiss = { showMapDialog = false }
        )
    }
}
