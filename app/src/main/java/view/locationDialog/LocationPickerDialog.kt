package view.locationDialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mapbox.geojson.Point
import com.example.fotogram.R
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage

@Composable
fun LocationPickerDialog(
    startLocation: Point,
    onDismiss: () -> Unit,
    onLocationSelected: (Point) -> Unit
) {
    // Posizione selezionata (inizia dalla posizione utente)
    var selectedLocation by remember { mutableStateOf(startLocation) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(startLocation)
            zoom(15.0)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi")
                    }

                    Text(
                        text = "Seleziona posizione",
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = { onLocationSelected(selectedLocation) }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Conferma",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Mappa
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = mapViewportState,
                        onMapClickListener = { point ->
                            selectedLocation = point
                            true
                        }
                    ) {
                        // Marker della posizione selezionata
                        val markerIcon = rememberIconImage(
                            key = "selected_marker",
                            painter = painterResource(R.drawable.post_location)
                        )

                        PointAnnotation(point = selectedLocation) {
                            iconImage = markerIcon
                            iconSize = 0.25
                        }
                    }
                }

                // Footer con coordinate
                Surface(
                    tonalElevation = 3.dp
                ) {
                    Text(
                        text = "Lat: %.5f, Lon: %.5f".format(
                            selectedLocation.latitude(),
                            selectedLocation.longitude()
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}