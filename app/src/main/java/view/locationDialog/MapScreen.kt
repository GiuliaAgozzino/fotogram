package view.locationDialog

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import utils.LocationManager
import view.location.LocationViewer
import view.location.RequestLocationPermission

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    targetLocation: Point,
    targetLabel: String = "Posizione",
    markerLabel: String? = null,
    maxDistanceKm: Double = 5.0,
    showDistanceInfo: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val locationManager = remember { LocationManager(context) }

    // Stati per la posizione utente
    var userLocation by remember { mutableStateOf<Point?>(null) }
    var isNear by remember { mutableStateOf<Boolean?>(null) }
    var distanceKm by remember { mutableStateOf<Double?>(null) }

    // Stati per permessi
    var requestPermission by remember { mutableStateOf(true) }
    var permissionGranted by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Richiedi permesso
    if (requestPermission) {
        RequestLocationPermission(
            onGranted = {
                Log.d("MapScreen", "Permesso concesso")
                permissionGranted = true
                requestPermission = false
                isLoadingLocation = true
            },
            onDenied = {
                Log.d("MapScreen", "Permesso negato")
                permissionGranted = false
                requestPermission = false
            },
            onDeniedForever = {
                Log.d("MapScreen", "Permesso negato per sempre")
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
            Log.d("MapScreen", "User location: $userLocation")

            // Calcola distanza
            val distance = locationManager.distanceInKm(
                lat, lon,
                targetLocation.latitude(),
                targetLocation.longitude()
            )
            distanceKm = distance
            isNear = distance <= maxDistanceKm

            Log.d("MapScreen", "Distanza: $distance km, isNear: $isNear")

            isLoadingLocation = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        targetLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // MAPPA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LocationViewer(
                    postLocation = targetLocation,
                    userLocation = userLocation,
                    isNear = isNear ?: false,
                    markerLabel = markerLabel
                )

                // Indicatore di caricamento
                if (isLoadingLocation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(shape = RoundedCornerShape(12.dp)) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Text("Ottenendo la tua posizione...")
                            }
                        }
                    }
                }
            }

            // INFO DISTANZA
            if (showDistanceInfo) {
                DistanceInfoCard(
                    distanceKm = distanceKm,
                    isNear = isNear,
                    userLocation = userLocation,
                    permissionGranted = permissionGranted,
                    isLoading = isLoadingLocation,
                    maxDistanceKm = maxDistanceKm
                )
            }
        }
    }
}

@Composable
private fun DistanceInfoCard(
    distanceKm: Double?,
    isNear: Boolean?,
    userLocation: Point?,
    permissionGranted: Boolean,
    isLoading: Boolean,
    maxDistanceKm: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !permissionGranted -> Color(0xFFFFF3E0)
                isNear == null || distanceKm == null ->
                    MaterialTheme.colorScheme.surfaceVariant
                !isNear -> Color(0xFFFFEBEE)
                else -> Color(0xFFE8F5E9)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when {
                !permissionGranted -> {
                    Text(
                        text = "⚠Permesso posizione negato",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "Abilita i permessi per vedere la distanza",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
                isLoading -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Calcolo distanza...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                distanceKm == null || isNear == null || userLocation == null -> {
                    Text(
                        text = "Calcolo in corso...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                !isNear -> {
                    Text(
                        text = "Sei troppo lontano",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Distanza: ${"%.1f".format(distanceKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "Devi essere entro ${"%.0f".format(maxDistanceKm)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
                else -> {
                    Text(
                        text = "Sei vicino!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "A circa ${"%.1f".format(distanceKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}