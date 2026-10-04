package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.GameMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class GameSessionTest {

    @Test
    fun initialSession_hasNoCallsNoAnnouncedNoWinners() {
        val session = GameSession(mode = GameMode.COLUMNA)

        assertTrue(session.calledNumbers.isEmpty())
        assertTrue(session.announced.isEmpty())
        assertTrue(session.winners.isEmpty())
    }

    @Test
    fun session_isImmutableValueType() {
        val session = GameSession(mode = GameMode.COLUMNA, calledNumbers = listOf(1, 2, 3))
        val copy = session.copy(calledNumbers = session.calledNumbers + 4)

        assertEquals(listOf(1, 2, 3), session.calledNumbers)
        assertEquals(listOf(1, 2, 3, 4), copy.calledNumbers)
    }
}
