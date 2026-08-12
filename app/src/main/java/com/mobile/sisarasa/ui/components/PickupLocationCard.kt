package com.mobile.sisarasa.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mobile.sisarasa.domain.model.Location
import com.mobile.sisarasa.BuildConfig

@Composable
fun PickupLocationCard(location: Location, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Lokasi Penjemputan",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = location.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (canEmbedMap) {
                EmbeddedMap(location = location, modifier = Modifier.fillMaxWidth().height(200.dp).padding(top = 12.dp))
            } else {
                OutlinedButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(geoUri(location))))
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text("Buka di Google Maps")
                }
            }
        }
    }
}

// ponytail: the public Maps Demo Key ships in Google's samples but does NOT support
// Maps SDK for Android (only the JS API + a few web services), so it can never render
// an embedded map. Treat it as unset and fall back to the deep link instead.
private const val PUBLIC_DEMO_KEY = "AIzaSyADsD6DFp7ovc_4RrYkdOfBob-i60yMhNU"
private val canEmbedMap: Boolean
    get() = BuildConfig.MAPS_API_KEY.isNotBlank() && BuildConfig.MAPS_API_KEY != PUBLIC_DEMO_KEY

private fun geoUri(location: Location): String {
    val address = location.address.trim()
    return if (address.isNotBlank()) {
        "geo:0,0?q=${Uri.encode(address)}"
    } else {
        "geo:${location.latitude},${location.longitude}?z=15"
    }
}

@Composable
private fun EmbeddedMap(location: Location, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val latLng = remember(location) { LatLng(location.latitude, location.longitude) }

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()
        mapView.getMapAsync { map ->
            map.addMarker(MarkerOptions().position(latLng).title(location.address))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}
