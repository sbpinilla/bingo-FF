package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.game.PredictionCandidate
import com.sergiodev.bingo.domain.repository.BoardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Live "Posibles ganadores" projection for the Play pane: a pure derivation of
 * `(boards, active game)`, sorted missing, then board id, then letter ordinal.
 */
class PlayPaneState(
    boards: BoardRepository,
    session: GameSessionHolder,
    scope: CoroutineScope,
) {
    val possibleWinners: StateFlow<List<PredictionCandidate>> =
        combine(boards.observeBoards(), session.active) { list, game ->
            visiblePossibleWinners(game, game?.toSession(list), list)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())
}
