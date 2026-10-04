package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplayTest {

    // Column-major: B1-5, I1-5, N1,N2,N4,N5, G1-5, O1-5
    private val board1 = BoardCard(
        id = 1L,
        identifier = "Casa1",
        numbers = listOf(
            3, 7, 12, 14, 15,
            16, 17, 18, 19, 20,
            31, 32, 34, 35,
            46, 47, 48, 49, 50,
            61, 62, 63, 64, 65,
        ),
    )

    private val board2 = BoardCard(
        id = 2L,
        identifier = "Casa2",
        numbers = listOf(
            1, 2, 4, 5, 6,
            21, 22, 23, 24, 25,
            33, 36, 37, 38,
            51, 52, 53, 54, 55,
            66, 67, 68, 69, 70,
        ),
    )

    @Test
    fun noCalls_producesEmptySessionForMode() {
        val session = replay(GameMode.COLUMNA, emptyList(), listOf(board1, board2))

        assertEquals(GameMode.COLUMNA, session.mode)
        assertTrue(session.calledNumbers.isEmpty())
        assertTrue(session.announced.isEmpty())
        assertTrue(session.winners.isEmpty())
    }

    @Test
    fun columnCompletion_announcesWinnerWithBoardDetails() {
        val called = listOf(3, 7, 12, 14, 15)

        val session = replay(GameMode.COLUMNA, called, listOf(board1, board2))

        assertEquals(called, session.calledNumbers)
        assertEquals(
            listOf(WinAnnouncement(boardId = 1L, sequentialNumber = 1L, identifier = "Casa1", patternId = "COLUMN_B")),
            session.winners,
        )
        assertEquals(setOf(AnnouncedWin(1L, "COLUMN_B")), session.announced)
    }

    @Test
    fun winnersAreAnnouncedOnceEvenWhenMoreNumbersAreCalledAfterwards() {
        val called = listOf(3, 7, 12, 14, 15, 99, 98)

        val session = replay(GameMode.COLUMNA, called, listOf(board1))

        assertEquals(1, session.winners.size)
    }

    @Test
    fun winnersKeepCallOrderAcrossBoardsAndPatterns() {
        // board2's B column completes first, then board1's B column, then board1's I column.
        val called = listOf(1, 2, 4, 5, 6, 3, 7, 12, 14, 15, 16, 17, 18, 19, 20)

        val session = replay(GameMode.COLUMNA, called, listOf(board1, board2))

        assertEquals(
            listOf(2L to "COLUMN_B", 1L to "COLUMN_B", 1L to "COLUMN_I"),
            session.winners.map { it.boardId to it.patternId },
        )
    }

    @Test
    fun isDeterministic_samePrefixGivesSameWinnersRegardlessOfLaterCalls() {
        val prefix = listOf(3, 7, 12, 14, 15)

        val short = replay(GameMode.COLUMNA, prefix, listOf(board1))
        val long = replay(GameMode.COLUMNA, prefix + listOf(16, 17), listOf(board1))

        assertEquals(short.winners, long.winners)
    }

    // --- dismissed (closed) COLUMNA letters void their column win ---

    @Test
    fun dismissedLetter_voidsThatColumnWinAndItsAnnouncedEntry() {
        val session = replay(GameMode.COLUMNA, listOf(3, 7, 12, 14, 15), listOf(board1), setOf(BingoLetter.B))

        assertTrue(session.winners.isEmpty())
        assertTrue(session.announced.isEmpty())
    }

    @Test
    fun dismissedLetter_keepsTheOtherWinsInTheirCallOrder() {
        // board2 B, then board1 B, then board1 I complete; closing B leaves only board1 I.
        val called = listOf(1, 2, 4, 5, 6, 3, 7, 12, 14, 15, 16, 17, 18, 19, 20)
        val boards = listOf(board1, board2)

        val withoutB = replay(GameMode.COLUMNA, called, boards, setOf(BingoLetter.B))
        val withoutI = replay(GameMode.COLUMNA, called, boards, setOf(BingoLetter.I))

        assertEquals(listOf(1L to "COLUMN_I"), withoutB.winners.map { it.boardId to it.patternId })
        assertEquals(setOf(AnnouncedWin(1L, "COLUMN_I")), withoutB.announced)
        assertEquals(
            listOf(2L to "COLUMN_B", 1L to "COLUMN_B"),
            withoutI.winners.map { it.boardId to it.patternId },
        )
    }

    @Test
    fun boardWithTwoColumnWins_keepsOnlyTheOneWhoseLetterIsOpen() {
        val called = listOf(3, 7, 12, 14, 15, 46, 47, 48, 49, 50)

        val session = replay(GameMode.COLUMNA, called, listOf(board1), setOf(BingoLetter.B))

        assertEquals(listOf("COLUMN_G"), session.winners.map { it.patternId })
    }

    @Test
    fun reopeningALetter_isTheSameReplayAsNeverDismissingIt() {
        val called = listOf(3, 7, 12, 14, 15, 46, 47, 48, 49, 50)

        val reopened = replay(GameMode.COLUMNA, called, listOf(board1), emptySet())

        assertEquals(replay(GameMode.COLUMNA, called, listOf(board1)), reopened)
        assertEquals(listOf("COLUMN_B", "COLUMN_G"), reopened.winners.map { it.patternId })
    }

    @Test
    fun dismissedLetters_areIgnoredOutsideColumna() {
        val lCalls = listOf(3, 7, 12, 14, 15, 20, 35, 50, 65)
        val fullCard = board1.numbers

        assertEquals(listOf("L"), replay(GameMode.L, lCalls, listOf(board1), setOf(BingoLetter.B)).winners.map { it.patternId })
        assertEquals(listOf("O"), replay(GameMode.O, fullCard, listOf(board1), setOf(BingoLetter.B)).winners.map { it.patternId })
        assertEquals(listOf("I"), replay(GameMode.I, fullCard, listOf(board1), setOf(BingoLetter.N)).winners.map { it.patternId })
        assertEquals(
            listOf("FULL_CARD"),
            replay(GameMode.CARTON_COMPLETO, fullCard, listOf(board1), BingoLetter.entries.toSet()).winners.map { it.patternId },
        )
    }
}
