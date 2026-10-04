package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode

/**
 * Rebuilds a [GameSession] from its persisted inputs. Only [mode] and
 * [calledNumbers] are stored; [GameSession.announced] and
 * [GameSession.winners] are derived by feeding [calledNumbers] one at a time,
 * in order, through [BingoWinChecker.newWins] so each (board, pattern) pair is
 * announced exactly once, at the call that completed it.
 */
fun replay(
    mode: GameMode,
    calledNumbers: List<Int>,
    boards: List<BoardCard>,
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
    return GameSession(mode = mode, calledNumbers = calledNumbers, announced = announced, winners = winners)
}
