package com.sergiodev.bingo.domain.game

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.model.GameMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class WinPredictionTest {

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
            1, 2, 4, 5, 6, // B
            21, 22, 23, 24, 25, // I
            33, 36, 37, 38, // N
            51, 52, 53, 54, 55, // G
            66, 67, 68, 69, 70, // O
        ),
    )

    @Test
    fun oLIModes_missingThreeOrLess_isIncluded() {
        listOf(GameMode.O, GameMode.L, GameMode.I).forEach { mode ->
            val pattern = mode.patterns.single()
            val realCells = pattern.cells.mapNotNull { board1.numberAt(it) }
            // Leave exactly 3 real cells uncalled -> missing == 3, at the qualifying threshold.
            val called = realCells.drop(3).toSet()

            val result = predictPossibleWinners(
                mode = mode,
                boards = listOf(board1),
                called = called,
                announced = emptySet(),
            )

            assertTrue(
                result.any { it.boardId == board1.id && it.missing == 3 },
                "mode=$mode should include board1 at missing==3",
            )
        }
    }

    @Test
    fun oLIModes_missingFour_isExcluded() {
        listOf(GameMode.O, GameMode.L, GameMode.I).forEach { mode ->
            val pattern = mode.patterns.single()
            val realCells = pattern.cells.mapNotNull { board1.numberAt(it) }
            // Leave exactly 4 real cells uncalled -> missing == 4, past the qualifying threshold.
            val called = realCells.drop(4).toSet()

            val result = predictPossibleWinners(
                mode = mode,
                boards = listOf(board1),
                called = called,
                announced = emptySet(),
            )

            assertTrue(
                result.none { it.boardId == board1.id },
                "mode=$mode should exclude board1 at missing==4",
            )
        }
    }

    @Test
    fun cartonCompleto_missingTenOrLess_isIncluded() {
        val pattern = GameMode.CARTON_COMPLETO.patterns.single()
        val realCells = pattern.cells.mapNotNull { board1.numberAt(it) }
        // Leave exactly 10 real cells uncalled -> missing == 10, at the qualifying threshold.
        val called = realCells.drop(10).toSet()

        val result = predictPossibleWinners(
            mode = GameMode.CARTON_COMPLETO,
            boards = listOf(board1),
            called = called,
            announced = emptySet(),
        )

        assertTrue(result.any { it.boardId == board1.id && it.missing == 10 })
    }

    @Test
    fun cartonCompleto_missingEleven_isExcluded() {
        val pattern = GameMode.CARTON_COMPLETO.patterns.single()
        val realCells = pattern.cells.mapNotNull { board1.numberAt(it) }
        // Leave exactly 11 real cells uncalled -> missing == 11, past the qualifying threshold.
        val called = realCells.drop(11).toSet()

        val result = predictPossibleWinners(
            mode = GameMode.CARTON_COMPLETO,
            boards = listOf(board1),
            called = called,
            announced = emptySet(),
        )

        assertTrue(result.none { it.boardId == board1.id })
    }

    @Test
    fun oLIAndCompleto_noCallCountCeiling_stillIncludedAtHighCallCount() {
        listOf(GameMode.O, GameMode.L, GameMode.I, GameMode.CARTON_COMPLETO).forEach { mode ->
            val pattern = mode.patterns.single()
            val realCells = pattern.cells.mapNotNull { board1.numberAt(it) }
            val threshold = if (mode == GameMode.CARTON_COMPLETO) 10 else 3
            // Leave exactly `threshold` real cells uncalled, then pad with unrelated
            // numbers so total distinct calls exceeds the old 6-call ceiling.
            val patternCalls = realCells.drop(threshold).toSet()
            val padding = (200 until 211).toSet()
            val called = patternCalls + padding

            val result = predictPossibleWinners(
                mode = mode,
                boards = listOf(board1),
                called = called,
                announced = emptySet(),
            )

            assertTrue(called.size > 6, "padding must push calls past the old 6-call ceiling")
            assertTrue(
                result.any { it.boardId == board1.id && it.missing == threshold },
                "mode=$mode should still qualify at high call counts, since no mode has a ceiling",
            )
        }
    }

    @Test
    fun nonColumnaModes_alreadyAnnouncedPair_isExcludedEvenIfStillQualifying() {
        listOf(GameMode.O, GameMode.L, GameMode.I, GameMode.CARTON_COMPLETO).forEach { mode ->
            val pattern = mode.patterns.single()
            val called = pattern.cells.mapNotNull { board1.numberAt(it) }.toSet() // fully completes the pattern
            val announced = setOf(AnnouncedWin(board1.id, pattern.id))

            val result = predictPossibleWinners(
                mode = mode,
                boards = listOf(board1),
                called = called,
                announced = announced,
            )

            assertTrue(
                result.none { it.boardId == board1.id },
                "mode=$mode already-announced pair must be excluded even if still qualifying",
            )
        }
    }

    @Test
    fun nonColumnaCandidate_hasNullLetter() {
        listOf(GameMode.O, GameMode.L, GameMode.I, GameMode.CARTON_COMPLETO).forEach { mode ->
            val pattern = mode.patterns.single()
            val called = pattern.cells.mapNotNull { board1.numberAt(it) }.toSet() // fully qualifies

            val result = predictPossibleWinners(
                mode = mode,
                boards = listOf(board1),
                called = called,
                announced = emptySet(),
            )

            assertTrue(result.isNotEmpty(), "mode=$mode should produce a candidate")
            assertTrue(result.all { it.letter == null }, "mode=$mode candidate letter must be null")
        }
    }

    @Test
    fun columna_exactlySixCalls_isIncluded() {
        // board1's B column: 3,7,12,14,15 — 5 calls, plus 1 unrelated call = 6 distinct.
        val called = setOf(3, 7, 12, 14, 46)
        val calledSix = called + 99 // 99 is out of 1..75 range but still a distinct called number

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board1),
            called = calledSix,
            announced = emptySet(),
        )

        assertEquals(6, calledSix.size)
        assertTrue(result.any { it.boardId == 1L && it.letter == BingoLetter.B })
    }

    @Test
    fun columna_noCallCountCeiling_stillIncludedAtHighCallCount() {
        // board1's B column: 3,7,12,14,15. Leave exactly 2 real cells (14, 15) uncalled -> missing == 2.
        val patternCalls = setOf(3, 7, 12)
        val padding = (200 until 211).toSet() // 11 unrelated numbers, well past the old 6-call ceiling
        val called = patternCalls + padding

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board1),
            called = called,
            announced = emptySet(),
        )

        assertTrue(called.size > 6, "padding must push calls past the old Columna-only ceiling")
        assertTrue(
                result.any { it.boardId == 1L && it.letter == BingoLetter.B && it.missing == 2 },
                "board1's B column should still qualify at high call counts, since Columna has no ceiling",
            )
    }

    @Test
    fun columna_belowThreshold_isExcluded() {
        // Only 2 of board1's B column numbers called -> missing 3, threshold requires missing <= 2.
        val called = setOf(3, 7)

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board1),
            called = called,
            announced = emptySet(),
        )

        assertTrue(result.none { it.boardId == 1L && it.letter == BingoLetter.B })
    }

    @Test
    fun alreadyAnnouncedPair_isExcludedEvenIfStillQualifying() {
        val called = setOf(3, 7, 12, 14, 15) // completes board1's B column
        val announced = setOf(AnnouncedWin(board1.id, "COLUMN_B"))

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board1),
            called = called,
            announced = announced,
        )

        assertTrue(result.none { it.boardId == 1L && it.letter == BingoLetter.B })
    }

    @Test
    fun boardQualifyingOnTwoColumns_producesTwoEntries() {
        // board1's B column: 3,7,12,14,15 ; board1's I column: 16,17,18,19,20.
        // Qualify both via missing <= 2 each: 3 of B's 5 and 3 of I's 5.
        val called = setOf(3, 7, 12, 16, 17, 18) // 6 distinct calls: B missing 2, I missing 2

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board1),
            called = called,
            announced = emptySet(),
        )

        assertTrue(result.any { it.boardId == 1L && it.letter == BingoLetter.B })
        assertTrue(result.any { it.boardId == 1L && it.letter == BingoLetter.I })
        assertEquals(2, result.count { it.boardId == 1L })
    }

    @Test
    fun results_sortedFewestMissingFirst_thenBoardId_thenLetterOrdinal() {
        // board1's N column (31,32,34,35 + FREE): 3 real numbers called -> missing 1.
        // board2's N column (33,36,37,38 + FREE): 2 real numbers called -> missing 2.
        val called = setOf(31, 32, 34, 33, 36) // 5 distinct calls

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board2, board1),
            called = called,
            announced = emptySet(),
        )

        assertEquals(2, result.size)
        // Fewest-missing-first: board1's N column (missing 1) sorts before board2's (missing 2).
        assertEquals(board1.id, result.first().boardId)
        assertEquals(BingoLetter.N, result.first().letter)
        assertEquals(1, result.first().missing)
        assertEquals(board2.id, result.last().boardId)
        assertEquals(2, result.last().missing)
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].missing <= result[i + 1].missing)
        }
    }

    @Test
    fun results_tiesOnMissing_breakByBoardIdThenLetterOrdinal() {
        // Both boards: B column missing 2 (board1 3,7,12 / board2 1,2,4), and board1 also I column missing 2.
        val called = setOf(3, 7, 12, 1, 2, 4, 16, 17, 18)

        val result = predictPossibleWinners(
            mode = GameMode.COLUMNA,
            boards = listOf(board2, board1),
            called = called,
            announced = emptySet(),
        )

        assertEquals(
            listOf(1L to BingoLetter.B, 1L to BingoLetter.I, 2L to BingoLetter.B),
            result.map { it.boardId to it.letter },
        )
        assertTrue(result.all { it.missing == 2 })
    }
}
