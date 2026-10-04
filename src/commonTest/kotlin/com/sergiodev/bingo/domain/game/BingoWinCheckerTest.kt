package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.model.GridPosition
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class BingoWinCheckerTest {

    // Column-major: B1-5, I1-5, N1,N2,N4,N5, G1-5, O1-5
    private val board1 = BoardCard(
        id = 1L,
        identifier = "Casa1",
        numbers = listOf(
            3, 7, 12, 14, 15, // B
            16, 17, 18, 19, 20, // I
            31, 32, 34, 35, // N
            46, 47, 48, 49, 50, // G
            61, 62, 63, 64, 65, // O
        ),
    )

    private val board2 = BoardCard(
        id = 2L,
        identifier = "Casa2",
        numbers = listOf(
            1, 2, 4, 5, 6, // B (disjoint from board1's B numbers)
            21, 22, 23, 24, 25, // I
            33, 36, 37, 38, // N
            51, 52, 53, 54, 55, // G
            66, 67, 68, 69, 70, // O
        ),
    )

    @Test
    fun isSatisfied_freeCellAlwaysSatisfied() {
        val pattern = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_N" }
        val freeCell = GridPosition(BingoLetter.N, 3)
        assertTrue(freeCell in pattern.cells)
        assertTrue(BingoWinChecker.isSatisfied(board1, emptySet(), pattern).let {
            // Even with zero calls, FREE alone is satisfied but the pattern needs all 5.
            !it
        })
    }

    @Test
    fun columnaWin_usesOnlyThatBoardsNumbers() {
        val columnB = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_B" }
        val called = setOf(3, 7, 12, 14, 15)

        assertTrue(BingoWinChecker.isSatisfied(board1, called, columnB))
        assertFalse(BingoWinChecker.isSatisfied(board2, called, columnB))
    }

    @Test
    fun nColumn_needsOnlyFourCallsDueToFree() {
        val columnN = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_N" }
        val called = setOf(31, 32, 34, 35)

        assertTrue(BingoWinChecker.isSatisfied(board1, called, columnN))
    }

    @Test
    fun oMode_requiresAllSixteenOuterCells() {
        val oPattern = GameMode.O.patterns.first()
        val allButOne = board1.numbers.dropLast(1).toSet()

        assertFalse(BingoWinChecker.isSatisfied(board1, allButOne, oPattern))
        assertTrue(BingoWinChecker.isSatisfied(board1, board1.numbers.toSet(), oPattern))
    }

    @Test
    fun lMode_requiresAllNineCells() {
        val lPattern = GameMode.L.patterns.first()
        val realCells = lPattern.cells.mapNotNull { board1.numberAt(it) }.toSet()
        val allButOne = realCells.toList().dropLast(1).toSet()

        assertFalse(BingoWinChecker.isSatisfied(board1, allButOne, lPattern))
        assertTrue(BingoWinChecker.isSatisfied(board1, realCells, lPattern))
    }

    @Test
    fun iMode_freeCellCountsButAllTwelveRealCellsStillRequired() {
        val iPattern = GameMode.I.patterns.first()
        val realCells = iPattern.cells.mapNotNull { board1.numberAt(it) }.toSet()
        val allButOne = realCells.toList().dropLast(1).toSet()

        assertFalse(BingoWinChecker.isSatisfied(board1, allButOne, iPattern))
        assertTrue(BingoWinChecker.isSatisfied(board1, realCells, iPattern))
    }

    @Test
    fun newWins_announcesEachNewlyCompletingBoardOncePerPattern() {
        val boards = listOf(board1, board2)
        val calledAfterBoard1Wins = board1.numbers.toSet()

        val firstWins = BingoWinChecker.newWins(
            boards = boards,
            called = calledAfterBoard1Wins,
            mode = GameMode.CARTON_COMPLETO,
            announced = emptySet(),
        )
        assertTrue(firstWins.any { it.boardId == board1.id })
        assertFalse(firstWins.any { it.boardId == board2.id })

        val announcedAfterFirst = firstWins.map { AnnouncedWin(it.boardId, it.patternId) }.toSet()

        // Re-running with the same called set and announced set must not re-announce board1.
        val repeat = BingoWinChecker.newWins(
            boards = boards,
            called = calledAfterBoard1Wins,
            mode = GameMode.CARTON_COMPLETO,
            announced = announcedAfterFirst,
        )
        assertTrue(repeat.isEmpty())

        val calledAfterBoard2Wins = calledAfterBoard1Wins + board2.numbers
        val secondWins = BingoWinChecker.newWins(
            boards = boards,
            called = calledAfterBoard2Wins,
            mode = GameMode.CARTON_COMPLETO,
            announced = announcedAfterFirst,
        )
        assertTrue(secondWins.any { it.boardId == board2.id })
        assertFalse(secondWins.any { it.boardId == board1.id })
    }

    @Test
    fun matchCount_zeroCalls_countsOnlyFreeCells() {
        val columnB = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_B" }
        val columnN = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_N" }

        assertEquals(0, BingoWinChecker.matchCount(board1, emptySet(), columnB))
        assertEquals(1, BingoWinChecker.matchCount(board1, emptySet(), columnN))
    }

    @Test
    fun matchCount_partialMatches_countsCalledPlusFree() {
        val columnN = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_N" }
        // board1's N numbers: 31, 32, 34, 35 (FREE at row 3)
        val called = setOf(31, 32)

        assertEquals(3, BingoWinChecker.matchCount(board1, called, columnN))
    }

    @Test
    fun matchCount_fullMatch_equalsPatternSizeAndSatisfiesIsSatisfied() {
        val columnB = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_B" }
        val called = setOf(3, 7, 12, 14, 15)

        assertEquals(columnB.cells.size, BingoWinChecker.matchCount(board1, called, columnB))
        assertTrue(BingoWinChecker.isSatisfied(board1, called, columnB))
    }

    @Test
    fun matchCount_freeCellAlwaysCountedRegardlessOfCalledNumbers() {
        val columnN = GameMode.COLUMNA.patterns.first { it.id == "COLUMN_N" }

        assertEquals(1, BingoWinChecker.matchCount(board1, emptySet(), columnN))
        assertEquals(1, BingoWinChecker.matchCount(board1, setOf(99), columnN))
    }
}
