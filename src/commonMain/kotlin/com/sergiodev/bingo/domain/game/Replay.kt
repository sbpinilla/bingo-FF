package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode

/**
 * Rebuilds a [GameSession] from its persisted inputs. Only [mode],
 * [calledNumbers] and [dismissedLetters] are stored; [GameSession.announced] and
 * [GameSession.winners] are derived by feeding [calledNumbers] one at a time,
 * in order, through [BingoWinChecker.newWins] so each (board, pattern) pair is
 * announced exactly once, at the call that completed it.
 *
 * In [GameMode.COLUMNA] a dismissed (closed) letter voids its column: the
 * COLUMN_X wins of every board are dropped from both [GameSession.winners] and
 * [GameSession.announced]. The surviving wins keep their call order. Because the
 * session is always derived from scratch, reopening the letter restores those
 * wins exactly as if it had never been dismissed. Other modes ignore
 * [dismissedLetters].
 */
fun replay(
    mode: GameMode,
    calledNumbers: List<Int>,
    boards: List<BoardCard>,
    dismissedLetters: Set<BingoLetter> = emptySet(),
): GameSession {
    var announced = emptySet<AnnouncedWin>()
    val winners = mutableListOf<WinAnnouncement>()
    val calledSoFar = mutableSetOf<Int>()
    for (number in calledNumbers) {
        calledSoFar += number
        val newWins = BingoWinChecker.newWins(boards, calledSoFar, mode, announced)
        winners += newWins
        announced = announced + newWins.map { AnnouncedWin(it.boardId, it.patternId) }
    }
    val voided = voidedPatternIds(mode, dismissedLetters)
    return GameSession(
        mode = mode,
        calledNumbers = calledNumbers,
        announced = announced.filterNot { it.patternId in voided }.toSet(),
        winners = winners.filterNot { it.patternId in voided },
    )
}

/** The COLUMNA column patterns whose letter is dismissed; always empty in other modes. */
private fun voidedPatternIds(mode: GameMode, dismissedLetters: Set<BingoLetter>): Set<String> =
    if (mode != GameMode.COLUMNA || dismissedLetters.isEmpty()) {
        emptySet()
    } else {
        mode.patterns.filter { it.cells.first().column in dismissedLetters }.map { it.id }.toSet()
    }
