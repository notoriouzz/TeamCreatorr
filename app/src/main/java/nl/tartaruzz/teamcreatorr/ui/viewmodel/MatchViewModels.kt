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
    val note: String = "",
    val selected: Set<Long> = emptySet(),
    val teamA: List<Long> = emptyList(),
    val teamB: List<Long> = emptyList(),
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

    fun setTeamSize(size: Int) {
        _state.value = _state.value.copy(
            teamSize = size,
            teamA = emptyList(),
            teamB = emptyList(),
            generationError = null
        )
    }

    fun setDate(millis: Long) {
        _state.value = _state.value.copy(date = millis)
    }

    fun setNote(text: String) {
        _state.value = _state.value.copy(note = text)
    }

    fun togglePlayer(id: Long) {
        val current = _state.value
        val selected = if (id in current.selected) current.selected - id else current.selected + id
        _state.value = current.copy(
            selected = selected,
            teamA = emptyList(),
            teamB = emptyList(),
            generationError = null
        )
    }

    fun generateTeams() {
        val current = _state.value
        if (!current.canGenerate) {
            _state.value = current.copy(
                teamA = emptyList(),
                teamB = emptyList(),
                generationError = "Selecteer precies ${current.required} spelers."
            )
            return
        }
        generate(current, duoPairs())
    }

    fun reshuffle() {
        generate(_state.value, duoPairs())
    }

    private fun duoPairs(): List<Pair<Long, Long>> =
        duos.value.map { it.playerA.id to it.playerB.id }

    private fun generate(current: NewMatchUiState, pairs: List<Pair<Long, Long>>) {
        when (val result = TeamGenerator.generate(current.selected.toList(), current.teamSize, pairs, Random.Default)) {
            is TeamGenerator.Result.Success -> _state.value = current.copy(
                teamA = result.split.teamA,
                teamB = result.split.teamB,
                generationError = null
            )
            is TeamGenerator.Result.Failure -> _state.value = current.copy(
                teamA = emptyList(),
                teamB = emptyList(),
                generationError = result.reason
            )
        }
    }

    fun confirm() {
        val current = _state.value
        if (current.confirming) return
        if (current.teamA.isEmpty() || current.teamB.isEmpty()) return
        _state.value = current.copy(confirming = true)
        viewModelScope.launch {
            val id = repo.createMatch(
                date = current.date,
                note = current.note,
                teamSize = current.teamSize,
                teamA = current.teamA,
                teamB = current.teamB
            )
            // Reset the form so the same match cannot be confirmed twice.
            _state.value = NewMatchUiState(confirmedMatchId = id)
        }
    }

    fun consumeConfirmation() {
        _state.value = _state.value.copy(confirmedMatchId = null)
    }

    fun addDuo(playerAId: Long, playerBId: Long, onResult: (Repository.DuoResult) -> Unit = {}) {
        viewModelScope.launch { onResult(repo.addDuo(playerAId, playerBId)) }
    }

    fun removeDuo(id: Long) {
        viewModelScope.launch { repo.removeDuo(id) }
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

    fun saveResult(id: Long, scoreA: Int, scoreB: Int) {
        viewModelScope.launch { repo.updateMatchResult(id, scoreA, scoreB) }
    }

    fun editDetails(id: Long, date: Long, note: String) {
        viewModelScope.launch { repo.updateMatchDetails(id, date, note) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteMatch(id) }
    }
}
