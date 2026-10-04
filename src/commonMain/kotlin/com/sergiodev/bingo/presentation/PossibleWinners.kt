package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.game.GameSession
import com.sergiodev.bingo.domain.game.PredictionCandidate
import com.sergiodev.bingo.domain.game.predictPossibleWinners
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.ActiveGame

/**
 * Near-win candidates shown to the user: the domain prediction (announced wins already excluded)
 * minus COLUMNA candidates whose letter is dismissed. Non-COLUMNA candidates have no letter and are
 * never filtered. A pure function of its inputs, so every call re-evaluates from scratch.
 */
fun visiblePossibleWinners(
    game: ActiveGame?,
    replayed: GameSession?,
    boards: List<BoardCard>,
): List<PredictionCandidate> {
    if (game == null || replayed == null) return emptyList()
    return predictPossibleWinners(game.mode, boards, game.calledNumbers.toSet(), replayed.announced)
        .filter { it.letter == null || it.letter !in game.dismissedLetters }
}
