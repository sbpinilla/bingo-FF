package com.sergiodev.bingo.domain.repository

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.GameMode

/**
 * The persisted state of the single active game. Winners and announcements are
 * never stored: they are rebuilt by replaying [calledNumbers] under [mode].
 * [dismissedLetters] holds the COLUMNA columns the user closed: they are hidden from the possible
 * winners list and their column wins are voided on replay.
 */
data class ActiveGame(
    val mode: GameMode,
    val calledNumbers: List<Int>,
    val dismissedLetters: Set<BingoLetter>,
)

/** Port for the single persisted active game. */
interface ActiveGameRepository {
    /** Returns the stored game, or `null` when none exists or the stored data is unusable. */
    suspend fun load(): ActiveGame?

    suspend fun save(game: ActiveGame)

    /** Removes the stored game. A no-op when none exists. */
    suspend fun clear()
}
