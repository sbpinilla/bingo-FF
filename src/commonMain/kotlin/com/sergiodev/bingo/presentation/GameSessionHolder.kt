package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.game.GameSession
import com.sergiodev.bingo.domain.game.replay
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import com.sergiodev.bingo.domain.repository.ActiveGameRepository
import com.sergiodev.bingo.domain.repository.BoardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Projection for the setup pane: whether a game can start and which mode is selected. */
data class GameSetupUiState(
    val hasBoards: Boolean = false,
    val availableModes: List<GameMode> = GameMode.entries.toList(),
    val selectedMode: GameMode? = null,
) {
    val canStart: Boolean get() = hasBoards && selectedMode != null
}

/**
 * Owns the single active game. The persisted [ActiveGame] is restored on construction;
 * every mutation is written through [repository] (in call order). Winners and announcements
 * are never stored: [session] rebuilds them by replaying the called numbers.
 */
class GameSessionHolder(
    boards: BoardRepository,
    private val repository: ActiveGameRepository,
    private val scope: CoroutineScope,
) {
    private val boardList = MutableStateFlow<List<BoardCard>>(emptyList())
    private val selectedMode = MutableStateFlow<GameMode?>(GameMode.COLUMNA)
    private val _active = MutableStateFlow<ActiveGame?>(null)
    private val _loaded = MutableStateFlow(false)
    private val persistLock = Mutex()

    /** The active game, or `null` when none is in progress. */
    val active: StateFlow<ActiveGame?> = _active

    /** `true` once the stored game (if any) has been read, so the UI can avoid a Setup flash. */
    val loaded: StateFlow<Boolean> = _loaded

    val setup: StateFlow<GameSetupUiState> = combine(boardList, selectedMode) { list, mode ->
        GameSetupUiState(hasBoards = list.isNotEmpty(), selectedMode = mode)
    }.stateIn(scope, SharingStarted.Eagerly, GameSetupUiState(selectedMode = GameMode.COLUMNA))

    /** The active game with winners and announced wins rebuilt by replaying its calls. */
    val session: StateFlow<GameSession?> = combine(_active, boardList) { game, list ->
        game?.let { replay(it.mode, it.calledNumbers, list) }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch { boards.observeBoards().collect { boardList.value = it } }
        scope.launch {
            val restored = persist { repository.load() }
            // A start() issued before the read finished wins over the stored game.
            if (_active.value == null && restored != null) _active.value = restored
            _loaded.value = true
        }
    }

    fun selectMode(mode: GameMode) {
        selectedMode.value = mode
    }

    /** Starts a game with [mode]. Ignored when a game is already active or no board exists. */
    fun start(mode: GameMode) {
        if (_active.value != null || boardList.value.isEmpty()) return
        val game = ActiveGame(mode, emptyList(), emptySet())
        _active.value = game
        scope.launch { persist { repository.save(game) } }
    }

    /** Ends the active game and clears the stored one. Boards are untouched. */
    fun end() {
        if (_active.value == null) return
        _active.value = null
        scope.launch { persist { repository.clear() } }
    }

    /** Serialises repository access; a storage failure must not crash the app, it only loses persistence. */
    private suspend fun <T> persist(block: suspend () -> T): T? =
        persistLock.withLock {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
}
