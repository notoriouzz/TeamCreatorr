package nl.tartaruzz.teamcreatorr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.tartaruzz.teamcreatorr.data.Repository
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.PlayerStats

data class HomeUiState(
    val totalPlayers: Int = 0,
    val activePlayers: Int = 0,
    val totalMatches: Int = 0,
    val topThree: List<PlayerStats> = emptyList(),
    val latestMatch: MatchDetail? = null,
    val loading: Boolean = true
)

class HomeViewModel(repo: Repository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.observePlayers(),
                repo.observeRanking(),
                repo.observeMatchDetails()
            ) { players, ranking, matches ->
                HomeUiState(
                    totalPlayers = players.size,
                    activePlayers = players.count { it.active },
                    totalMatches = matches.size,
                    topThree = ranking.filter { it.active }.take(3),
                    latestMatch = matches.firstOrNull(),
                    loading = false
                )
            }.collect { _state.value = it }
        }
    }
}

class RankingViewModel(repo: Repository) : ViewModel() {
    val ranking: StateFlow<List<PlayerStats>> = repo.observeRanking()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class HistoryViewModel(repo: Repository) : ViewModel() {
    val matches: StateFlow<List<MatchDetail>> = repo.observeMatchDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
