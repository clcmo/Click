package clcmo.com.br.click.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import clcmo.com.br.click.model.Coordinates
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Fonte de localização. Quem coleta o Flow liga as atualizações; ao parar de coletar, elas são removidas. */
interface LocationRepository {
    val locations: Flow<Coordinates>
}

class FusedLocationRepository(context: Context) : LocationRepository {

    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L).build()

    // A UI só começa a coletar depois que a permissão foi concedida.
    @SuppressLint("MissingPermission")
    override val locations: Flow<Coordinates> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(Coordinates(it.latitude, it.longitude)) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
