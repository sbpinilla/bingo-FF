package com.sergiodev.bingo.presentation

import androidx.compose.runtime.Immutable
import com.sergiodev.bingo.domain.game.replay
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GridPosition
import com.sergiodev.bingo.domain.repository.BoardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One cell of a compact card. [number] is `null` for the FREE centre, which is never a call mark. */
@Immutable
data class CellState(val number: Int?, val marked: Boolean)

/**
 * Precomputed render state of one board card. [cells] are row-major (row 1 B..O, row 2 B..O, ...).
 * [winningPatternIds] lists every pattern of the active mode this board has completed.
 * [missing] is the fewest cells still needed by any near-win candidate of this board (after the
 * dismissed-letter filter), or `null` when the board is not close to winning.
 */
@Immutable
data class BoardCardState(
    val id: Long,
    val identifier: String,
    val cells: List<CellState>,
    val winningPatternIds: List<String>,
    val missing: Int? = null,
) {
    val isWinner: Boolean get() = winningPatternIds.isNotEmpty()
}

/**
 * Live left-pane projection: for every board, which cells are called and which patterns it has won.
 * A pure derivation of `(boards, active game)`; with no active game nothing is marked.
 */
class BoardsPaneState(
    boards: BoardRepository,
    session: GameSessionHolder,
    scope: CoroutineScope,
) {
    val cards: StateFlow<List<BoardCardState>> = combine(boards.observeBoards(), session.active) { list, game ->
        val called = game?.calledNumbers.orEmpty().toSet()
        val replayed = game?.let { replay(it.mode, it.calledNumbers, list) }
        val winnersByBoard = replayed?.winners.orEmpty().groupBy({ it.boardId }, { it.patternId })
        // Candidates arrive sorted by missing, so the first one per board is its closest.
        val missingByBoard = visiblePossibleWinners(game, replayed, list)
            .groupBy { it.boardId }
            .mapValues { (_, candidates) -> candidates.first().missing }
        list.map { board -> board.toCardState(called, winnersByBoard[board.id].orEmpty(), missingByBoard[board.id]) }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())
}

private const val BOARD_SIZE = 5

private fun BoardCard.toCardState(called: Set<Int>, winningPatternIds: List<String>, missing: Int?) = BoardCardState(
    id = id,
    identifier = identifier,
    cells = (1..BOARD_SIZE).flatMap { row ->
        BingoLetter.entries.map { letter ->
            val number = numberAt(GridPosition(letter, row))
            CellState(number = number, marked = number != null && number in called)
        }
    },
    winningPatternIds = winningPatternIds,
    missing = missing,
)
