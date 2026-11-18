package clcmo.com.br.click.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clcmo.com.br.click.data.LocationRepository
import clcmo.com.br.click.model.Checkin
import clcmo.com.br.click.model.Coordinates
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapsViewModel(
    private val locations: LocationRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    data class UiState(
        val isMapReady: Boolean = false,
        val checkins: List<Checkin> = emptyList(),
    )

    /** Acontecimentos pontuais: a UI reage uma vez (toast, abrir câmera). */
    sealed interface Event {
        data object MapNotReady : Event
        data object NoGpsSignal : Event
        data object OpenCamera : Event
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private var currentLocation: Coordinates? = null
    private var pendingCheckin: Checkin? = null
    private var updatesJob: Job? = null

    fun startLocationUpdates() {
        if (updatesJob?.isActive == true) return
        updatesJob = viewModelScope.launch {
            locations.locations.collect { currentLocation = it }
        }
    }

    fun stopLocationUpdates() {
        updatesJob?.cancel()
        updatesJob = null
    }

    fun onMapReady() = _uiState.update { it.copy(isMapReady = true) }

    fun onCheckinClicked() {
        if (!_uiState.value.isMapReady) {
            _events.trySend(Event.MapNotReady)
            return
        }
        val here = currentLocation
        if (here == null) _events.trySend(Event.NoGpsSignal)
        pendingCheckin = Checkin(here ?: AV_PAULISTA, clock())
        _events.trySend(Event.OpenCamera)
    }

    /** O check-in só vale se a foto foi tirada. */
    fun onPhotoTaken(taken: Boolean) {
        val checkin = pendingCheckin ?: return
        pendingCheckin = null
        if (taken) _uiState.update { it.copy(checkins = it.checkins + checkin) }
    }

    companion object {
        val AV_PAULISTA = Coordinates(-23.5631338, -46.6543286)
    }
}
