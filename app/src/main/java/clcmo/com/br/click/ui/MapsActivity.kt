package clcmo.com.br.click.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import clcmo.com.br.click.R
import clcmo.com.br.click.data.FusedLocationRepository
import clcmo.com.br.click.databinding.ActivityMapsBinding
import clcmo.com.br.click.model.Coordinates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.Date

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private var map: GoogleMap? = null

    private val viewModel: MapsViewModel by viewModels {
        viewModelFactory { initializer { MapsViewModel(FusedLocationRepository(applicationContext)) } }
    }

    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (hasLocationPermission()) viewModel.startLocationUpdates()
        }

    private val camera =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo ->
            viewModel.onPhotoTaken(photo != null)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        binding.checkin.setOnClickListener { viewModel.onCheckinClicked() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handle) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (hasLocationPermission()) {
            viewModel.startLocationUpdates()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
            ) toast(R.string.permissao_GPS)
            locationPermission.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        viewModel.onMapReady()
        render(viewModel.uiState.value)
    }

    private fun render(state: MapsViewModel.UiState) {
        val map = map ?: return
        map.clear()
        state.checkins.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(it.coordinates.toLatLng())
                    .title(DateFormat.getTimeFormat(this).format(Date(it.timestamp)))
            )
        }
        state.checkins.lastOrNull()?.let {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.coordinates.toLatLng(), 16f))
        }
    }

    private fun handle(event: MapsViewModel.Event) = when (event) {
        MapsViewModel.Event.MapNotReady -> toast(R.string.carregando_Mapa)
        MapsViewModel.Event.NoGpsSignal -> toast(R.string.falta_sinal_GPS)
        MapsViewModel.Event.OpenCamera -> camera.launch(null)
    }

    private fun hasLocationPermission() = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun toast(@StringRes message: Int) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun Coordinates.toLatLng() = LatLng(latitude, longitude)
}
