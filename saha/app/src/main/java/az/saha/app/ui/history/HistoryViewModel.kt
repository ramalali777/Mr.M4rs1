package az.saha.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import az.saha.app.data.model.Measurement
import az.saha.app.data.repository.MeasurementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val items: List<Measurement> = emptyList(),
    val syncing: Boolean = false,
    val message: String? = null
)

class HistoryViewModel(
    private val repository: MeasurementRepository,
    private val userIdProvider: () -> String?
) : ViewModel() {

    private val userId = MutableStateFlow(userIdProvider())

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<Measurement>> = userId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList()) else repository.observe(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    fun refreshUser() {
        userId.value = userIdProvider()
    }

    fun sync() {
        val id = userIdProvider() ?: return
        viewModelScope.launch {
            _syncing.value = true
            runCatching { repository.sync(id) }
            _syncing.value = false
        }
    }

    fun delete(item: Measurement) {
        viewModelScope.launch {
            repository.delete(item.userId, item.id)
        }
    }

    companion object {
        fun factory(
            repo: MeasurementRepository,
            userIdProvider: () -> String?
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(repo, userIdProvider) as T
            }
        }
    }
}
