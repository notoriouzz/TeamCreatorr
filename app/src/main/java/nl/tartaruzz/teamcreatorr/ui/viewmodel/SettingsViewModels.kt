package nl.tartaruzz.teamcreatorr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.PlayerStats
import nl.tartaruzz.teamcreatorr.domain.PointSettings

class SettingsViewModel(private val repo: Repository) : ViewModel() {

    val settings: StateFlow<PointSettings> = repo.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PointSettings(1, 2, 1))

    val ranking: StateFlow<List<PlayerStats>> = repo.observeRanking()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val matches: StateFlow<List<MatchDetail>> = repo.observeMatchDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSettings(attendancePoints: Int, winPoints: Int, drawPoints: Int) {
        viewModelScope.launch { repo.updateSettings(attendancePoints, winPoints, drawPoints) }
    }

    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch { onResult(runCatching { repo.exportBackupJson() }.getOrNull()) }
    }

    fun importBackup(json: String, replace: Boolean, onResult: (Repository.ImportResult) -> Unit) {
        viewModelScope.launch { onResult(repo.importBackupJson(json, replace)) }
    }

    fun resetAll(onDone: () -> Unit = {}) = viewModelScope.launch { repo.resetAll(); onDone() }
    fun resetMatches(onDone: () -> Unit = {}) = viewModelScope.launch { repo.resetMatches(); onDone() }
    fun resetPlayers(onDone: () -> Unit = {}) = viewModelScope.launch { repo.resetPlayers(); onDone() }
}
