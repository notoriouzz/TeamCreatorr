package nl.tartaruzz.teamcreatorr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.tartaruzz.teamcreatorr.data.PlayerEntity
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.domain.PlayerProfileUiData

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerListViewModel(private val repo: Repository) : ViewModel() {

    val players: StateFlow<List<PlayerEntity>> = repo.observePlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.addPlayer(name) }
    }

    fun addMany(names: List<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isNotEmpty()) viewModelScope.launch { repo.addPlayers(clean) }
    }

    fun rename(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renamePlayer(id, name) }
    }

    fun setActive(id: Long, active: Boolean) {
        viewModelScope.launch { repo.setPlayerActive(id, active) }
    }

    fun delete(id: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.deletePlayer(id)) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerProfileViewModel(private val repo: Repository) : ViewModel() {

    private val idFlow = MutableStateFlow<Long?>(null)

    val profile: StateFlow<PlayerProfileUiData?> = idFlow
        .filterNotNull()
        .flatMapLatest { repo.observePlayerProfile(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun load(id: Long) {
        idFlow.value = id
    }

    fun rename(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renamePlayer(id, name) }
    }

    fun setActive(id: Long, active: Boolean) {
        viewModelScope.launch { repo.setPlayerActive(id, active) }
    }
}
