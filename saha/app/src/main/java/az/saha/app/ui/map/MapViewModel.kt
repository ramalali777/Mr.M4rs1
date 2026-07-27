package az.saha.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.saha.app.data.model.Measurement
import az.saha.app.data.repository.MeasurementRepository
import az.saha.app.domain.AreaCalculator
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import az.saha.app.location.LocationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MeasureMode { TAP, WALK }

data class MapUiState(
    val points: List<GeoPoint> = emptyList(),
    val unit: AreaUnit = AreaUnit.SOT,
    val mode: MeasureMode = MeasureMode.TAP,
    val walkTracking: Boolean = false,
    val currentLocation: GeoPoint? = null,
    val areaM2: Double = 0.0,
    val perimeterM: Double = 0.0,
    val message: String? = null,
    val saving: Boolean = false,
    val saveTitle: String = "",
    val showSaveDialog: Boolean = false
)

class MapViewModel(
    private val measurementRepository: MeasurementRepository,
    private val locationTracker: LocationTracker,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val _ui = MutableStateFlow(MapUiState())
    val ui: StateFlow<MapUiState> = _ui.asStateFlow()

    private var walkJob: Job? = null
    private var followJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { locationTracker.lastKnown() }.getOrNull()?.let { point ->
                _ui.update { it.copy(currentLocation = point) }
            }
        }
    }

    /** Keep currentLocation fresh for map centering / my-location (not measurement). */
    fun startLocationFollow() {
        if (followJob?.isActive == true) return
        followJob = viewModelScope.launch {
            runCatching {
                locationTracker.locationUpdates(intervalMs = 2500L).collect { point ->
                    _ui.update { it.copy(currentLocation = point) }
                }
            }
        }
    }

    fun stopLocationFollow() {
        followJob?.cancel()
        followJob = null
    }

    fun setUnit(unit: AreaUnit) = _ui.update { it.copy(unit = unit) }

    fun setMode(mode: MeasureMode) {
        if (mode != MeasureMode.WALK) stopWalk()
        _ui.update { it.copy(mode = mode, message = null) }
    }

    fun addTapPoint(point: GeoPoint) {
        if (_ui.value.mode != MeasureMode.TAP) return
        appendPoint(point)
    }

    fun recordWalkPoint() {
        val loc = _ui.value.currentLocation ?: return
        appendPoint(loc)
    }

    private fun appendPoint(point: GeoPoint) {
        val next = _ui.value.points + point
        recompute(next)
    }

    fun undo() {
        val next = _ui.value.points.dropLast(1)
        recompute(next)
    }

    fun clear() {
        stopWalk()
        _ui.update {
            it.copy(
                points = emptyList(),
                areaM2 = 0.0,
                perimeterM = 0.0,
                message = null,
                walkTracking = false
            )
        }
    }

    fun startWalk() {
        if (_ui.value.walkTracking) return
        _ui.update { it.copy(mode = MeasureMode.WALK, walkTracking = true, message = "İzləmə aktiv") }
        walkJob?.cancel()
        walkJob = viewModelScope.launch {
            locationTracker.locationUpdates().collect { point ->
                _ui.update { state ->
                    val shouldAutoAdd = state.points.isEmpty() ||
                        distanceMeters(state.points.last(), point) >= 3.0
                    val points = if (shouldAutoAdd) state.points + point else state.points
                    val area = if (points.size >= 3) AreaCalculator.areaSquareMeters(points) else 0.0
                    val peri = AreaCalculator.openPathLengthMeters(points)
                    state.copy(
                        currentLocation = point,
                        points = points,
                        areaM2 = area,
                        perimeterM = peri
                    )
                }
            }
        }
    }

    fun stopWalk() {
        walkJob?.cancel()
        walkJob = null
        _ui.update { it.copy(walkTracking = false) }
    }

    fun finishWalk() {
        stopWalk()
        val points = _ui.value.points
        if (points.size < 3) {
            _ui.update { it.copy(message = "Ən azı 3 nöqtə lazımdır") }
            return
        }
        recompute(points)
        _ui.update { it.copy(message = "Ölçmə tamamlandı") }
    }

    fun openSaveDialog() {
        if (_ui.value.points.size < 3) {
            _ui.update { it.copy(message = "Saxlamaq üçün ən azı 3 nöqtə seçin") }
            return
        }
        _ui.update {
            it.copy(
                showSaveDialog = true,
                saveTitle = "Ölçmə ${it.points.size} nöqtə"
            )
        }
    }

    fun onSaveTitle(v: String) = _ui.update { it.copy(saveTitle = v) }
    fun dismissSave() = _ui.update { it.copy(showSaveDialog = false) }

    fun save() {
        val userId = userIdProvider()
        if (userId.isNullOrBlank()) {
            _ui.update { it.copy(message = "Saxlamaq üçün daxil olun") }
            return
        }
        val state = _ui.value
        if (state.points.size < 3) return

        viewModelScope.launch {
            _ui.update { it.copy(saving = true) }
            val measurement = Measurement(
                userId = userId,
                title = state.saveTitle.ifBlank { "Adsız sahə" },
                points = state.points,
                areaSquareMeters = state.areaM2,
                perimeterMeters = AreaCalculator.closedPerimeterMeters(state.points),
                preferredUnit = state.unit
            )
            runCatching { measurementRepository.save(measurement) }
                .onSuccess {
                    _ui.update {
                        it.copy(
                            saving = false,
                            showSaveDialog = false,
                            message = "Saxlanıldı ✓"
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            saving = false,
                            message = e.localizedMessage ?: "Saxlama xətası"
                        )
                    }
                }
        }
    }

    private fun recompute(points: List<GeoPoint>) {
        val area = if (points.size >= 3) AreaCalculator.areaSquareMeters(points) else 0.0
        val peri = if (points.size >= 3) {
            AreaCalculator.closedPerimeterMeters(points)
        } else {
            AreaCalculator.openPathLengthMeters(points)
        }
        _ui.update {
            it.copy(points = points, areaM2 = area, perimeterM = peri, message = null)
        }
    }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        return AreaCalculator.openPathLengthMeters(listOf(a, b))
    }

    override fun onCleared() {
        stopWalk()
        stopLocationFollow()
        super.onCleared()
    }

    companion object {
        fun factory(
            repo: MeasurementRepository,
            tracker: LocationTracker,
            userIdProvider: () -> String?
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(repo, tracker, userIdProvider) as T
            }
        }
    }
}
