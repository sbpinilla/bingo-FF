package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode

/**
 * A (board, column) pair close to winning. [missing] is how many of the
 * pattern's cells are still unsatisfied.
 */
data class PredictionCandidate(
    val boardId: Long,
    val identifier: String,
    val letter: BingoLetter?,
    val missing: Int,
)

/**
 * Per-mode missing-cell threshold below which a (board, pattern) pair
 * qualifies for "Posibles ganadores": COLUMNA needs at most 2 cells left,
 * O/L/I need at most 3, and CARTON_COMPLETO needs at most 10.
 */
private fun missingThreshold(mode: GameMode): Int = when (mode) {
    GameMode.COLUMNA -> 2
    GameMode.O, GameMode.L, GameMode.I -> 3
    GameMode.CARTON_COMPLETO -> 10
}

/**
 * Near-win candidates for every [GameMode], re-evaluated on every newly
 * called number. No [GameMode] is subject to a session-wide call-count
 * ceiling: a (board, pattern) pair qualifies purely on its own missing-cell
 * count, however many unrelated numbers have been called elsewhere — a
 * column sitting at missing <= 2 stays a valid near-win no matter how many
 * numbers from other columns were called in between.
 *
 * A pair already present in [announced] is excluded even if it still
 * numerically satisfies the qualifying rule. Results are sorted
 * fewest-missing-first, then by board id, then by column ordinal (COLUMNA
 * candidates before any tie without a letter), for deterministic order.
 */
fun predictPossibleWinners(
    mode: GameMode,
    boards: List<BoardCard>,
    called: Set<Int>,
    announced: Set<AnnouncedWin>,
): List<PredictionCandidate> {
    val threshold = missingThreshold(mode)
    val candidates = mutableListOf<PredictionCandidate>()
    for (board in boards) {
        for (pattern in mode.patterns) {
            if (AnnouncedWin(board.id, pattern.id) in announced) continue
            val missing = pattern.cells.size - BingoWinChecker.matchCount(board, called, pattern)
            if (missing > threshold) continue
            val letter = if (mode == GameMode.COLUMNA) pattern.cells.first().column else null
            candidates += PredictionCandidate(
                boardId = board.id,
                identifier = board.identifier,
                letter = letter,
                missing = missing,
            )
        }
    }
    return candidates.sortedWith(compareBy({ it.missing }, { it.boardId }, { it.letter?.ordinal ?: 0 }))
}
