package clcmo.com.br.click.ui

import clcmo.com.br.click.data.LocationRepository
import clcmo.com.br.click.model.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapsViewModelTest {

    private class FakeLocations(
        val source: MutableSharedFlow<Coordinates> = MutableSharedFlow(),
    ) : LocationRepository {
        override val locations: Flow<Coordinates> = source
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `checkin com mapa nao pronto avisa e nao abre a camera`() = runTest {
        val vm = MapsViewModel(FakeLocations())
        vm.onCheckinClicked()
        assertEquals(MapsViewModel.Event.MapNotReady, vm.events.first())
    }

    @Test
    fun `sem GPS usa a Paulista e ainda abre a camera`() = runTest {
        val vm = MapsViewModel(FakeLocations())
        vm.onMapReady()
        vm.onCheckinClicked()
        assertEquals(
            listOf(MapsViewModel.Event.NoGpsSignal, MapsViewModel.Event.OpenCamera),
            vm.events.take(2).toList(),
        )
        vm.onPhotoTaken(true)
        assertEquals(MapsViewModel.AV_PAULISTA, vm.uiState.value.checkins.single().coordinates)
    }

    @Test
    fun `checkin usa a ultima localizacao e so conta com foto`() = runTest {
        val fake = FakeLocations()
        val vm = MapsViewModel(fake)
        vm.onMapReady()
        vm.startLocationUpdates()
        val here = Coordinates(-23.5, -46.6)
        fake.source.emit(here)

        vm.onCheckinClicked()
        vm.onPhotoTaken(false)
        assertEquals(0, vm.uiState.value.checkins.size)

        vm.onCheckinClicked()
        vm.onPhotoTaken(true)
        assertEquals(here, vm.uiState.value.checkins.single().coordinates)
    }
}
