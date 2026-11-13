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
import nl.tartaruzz.teamcreatorr.domain.DuoWithNames
import nl.tartaruzz.teamcreatorr.domain.MatchDetail
import nl.tartaruzz.teamcreatorr.domain.TeamGenerator
import kotlin.random.Random

data class NewMatchUiState(
    val teamSize: Int = 5,
    val date: Long = System.currentTimeMillis(),
    val selected: Set<Long> = emptySet(),
    val teamA: List<Long> = emptyList(),
    val teamB: List<Long> = emptyList(),
    val teamARating: Int = 0,
    val teamBRating: Int = 0,
    val useRanking: Boolean = false,
    val disabledDuoIds: Set<Long> = emptySet(),
    val generationError: String? = null,
    val confirming: Boolean = false,
    val confirmedMatchId: Long? = null
) {
    val required: Int get() = teamSize * 2
    val canGenerate: Boolean get() = selected.size == required
}

class NewMatchViewModel(private val repo: Repository) : ViewModel() {

    private val _state = MutableStateFlow(NewMatchUiState())
    val state: StateFlow<NewMatchUiState> = _state.asStateFlow()

    val activePlayers: StateFlow<List<PlayerEntity>> = repo.observeActivePlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val duos: StateFlow<List<DuoWithNames>> = repo.observeDuos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ratings: StateFlow<Map<Long, Int>> = repo.observeRatings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun setTeamSize(size: Int) {
        _state.value = _state.value.copy(
            teamSize = size,
            teamA = emptyList(),
            teamB = emptyList(),
            teamARating = 0,
            teamBRating = 0,
            generationError = null
        )
    }

    fun setDate(millis: Long) = _state.value.let { _state.value = it.copy(date = millis) }

    fun setUseRanking(use: Boolean) = _state.value.let {
        _state.value = it.copy(useRanking = use, teamA = emptyList(), teamB = emptyList(), generationError = null)
    }

    fun togglePlayer(id: Long) {
        val current = _state.value
        val selected = if (id in current.selected) current.selected - id else current.selected + id
        _state.value = current.copy(selected = selected, teamA = emptyList(), teamB = emptyList(), generationError = null)
    }

    fun selectAll() {
        val all = activePlayers.value.map { it.id }.toSet()
        _state.value = _state.value.copy(selected = all, teamA = emptyList(), teamB = emptyList(), generationError = null)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selected = emptySet(), teamA = emptyList(), teamB = emptyList(), generationError = null)
    }

    fun toggleDuo(id: Long) {
        val current = _state.value
        val disabled = if (id in current.disabledDuoIds) current.disabledDuoIds - id else current.disabledDuoIds + id
        _state.value = current.copy(disabledDuoIds = disabled, teamA = emptyList(), teamB = emptyList(), generationError = null)
    }

    fun addDuo(a: Long, b: Long, onResult: (Repository.DuoResult) -> Unit) {
        viewModelScope.launch {
            val result = repo.addDuo(a, b)
            if (result is Repository.DuoResult.Success) {
                _state.value = _state.value.copy(teamA = emptyList(), teamB = emptyList(), generationError = null)
            }
            onResult(result)
        }
    }

    fun removeDuo(id: Long) {
        _state.value = _state.value.copy(teamA = emptyList(), teamB = emptyList(), generationError = null)
        viewModelScope.launch { repo.removeDuo(id) }
    }

    fun effectiveDuos(): List<Pair<Long, Long>> =
        duos.value
            .filter { it.id !in _state.value.disabledDuoIds }
            .map { it.playerA.id to it.playerB.id }

    fun generateTeams() {
        val current = _state.value
        if (!current.canGenerate) {
            _state.value = current.copy(
                teamA = emptyList(), teamB = emptyList(),
                generationError = "Selecteer precies ${current.required} spelers."
            )
            return
        }
        generate(current)
    }

    fun reshuffle() = generate(_state.value)

    private fun generate(current: NewMatchUiState) {
        val duos = effectiveDuos()
        val result = if (current.useRanking) {
            TeamGenerator.generateBalanced(current.selected.toList(), current.teamSize, duos, ratings.value, Random.Default)
        } else {
            TeamGenerator.generate(current.selected.toList(), current.teamSize, duos, Random.Default)
        }
        when (result) {
            is TeamGenerator.Result.Success -> _state.value = current.copy(
                teamA = result.split.teamA,
                teamB = result.split.teamB,
                teamARating = result.teamARating,
                teamBRating = result.teamBRating,
                generationError = null
            )
            is TeamGenerator.Result.Failure -> _state.value = current.copy(
                teamA = emptyList(), teamB = emptyList(), teamARating = 0, teamBRating = 0,
                generationError = result.reason
            )
        }
    }

    fun confirm() {
        val current = _state.value
        if (current.confirming || current.confirmedMatchId != null) return
        if (current.teamA.isEmpty() || current.teamB.isEmpty()) return
        _state.value = current.copy(confirming = true)
        viewModelScope.launch {
            val id = repo.createMatch(
                date = current.date,
                teamSize = current.teamSize,
                teamA = current.teamA,
                teamB = current.teamB,
                duos = effectiveDuos()
            )
            _state.value = _state.value.copy(confirming = false, confirmedMatchId = id)
        }
    }

    fun consumeConfirmation() {
        _state.value = _state.value.copy(confirmedMatchId = null)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MatchDetailViewModel(private val repo: Repository) : ViewModel() {

    private val idFlow = MutableStateFlow<Long?>(null)

    val match: StateFlow<MatchDetail?> = idFlow
        .filterNotNull()
        .flatMapLatest { repo.observeMatchDetail(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun load(id: Long) {
        idFlow.value = id
    }

    fun markPlayed(id: Long) {
        viewModelScope.launch { repo.markMatchPlayed(id) }
    }

    fun saveResult(id: Long, scoreA: Int, scoreB: Int) {
        viewModelScope.launch { repo.updateMatchResult(id, scoreA, scoreB) }
    }

    fun editDetails(id: Long, date: Long) {
        viewModelScope.launch { repo.updateMatchDetails(id, date) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteMatch(id) }
    }
}

