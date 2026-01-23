package view.location


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.fotogram.R
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage


@Composable
fun LocationViewer(
    postLocation: Point,
    userLocation: Point?=null,
    isNear: Boolean,
    modifier: Modifier = Modifier
) {
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(postLocation)
            zoom(14.0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        ) {

            val postMarker = rememberIconImage(
                key = R.drawable.post_location,
                painter = painterResource(R.drawable.post_location)
            )

            val userMarker = rememberIconImage(
                key = R.drawable.user_location,
                painter = painterResource(R.drawable.user_location)
            )

            // Marker del post (sempre)
            PointAnnotation(
                point = postLocation
            ) {
                iconImage = postMarker
                iconSize = 0.25
            }

            // Marker dell’utente (solo se vicino)
            if (userLocation != null) {
                if (isNear && userLocation.latitude() != 0.0 && userLocation.longitude() != 0.0) {
                    PointAnnotation(
                        point = userLocation
                    ) {
                        iconImage = userMarker
                        iconSize = 0.25
                    }
                }
            }
        }
    }
}
