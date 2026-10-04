package com.sergiodev.bingo.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class GameModeTest {

    @Test
    fun columna_hasFivePatternsOfFiveCellsEach() {
        val patterns = GameMode.COLUMNA.patterns
        assertEquals(5, patterns.size)
        patterns.forEach { assertEquals(5, it.cells.size) }
    }

    @Test
    fun columna_nColumnPatternIncludesFreeCell() {
        val nPattern = GameMode.COLUMNA.patterns.first { pattern ->
            pattern.cells.all { it.column == BingoLetter.N }
        }
        assertTrue(nPattern.cells.contains(GridPosition(BingoLetter.N, 3)))
    }

    @Test
    fun o_hasOnePatternOfSixteenCells() {
        val patterns = GameMode.O.patterns
        assertEquals(1, patterns.size)
        assertEquals(16, patterns.first().cells.size)
    }

    @Test
    fun l_hasOnePatternOfNineCells() {
        val patterns = GameMode.L.patterns
        assertEquals(1, patterns.size)
        assertEquals(9, patterns.first().cells.size)
    }

    @Test
    fun i_hasOnePatternOfThirteenCells() {
        val patterns = GameMode.I.patterns
        assertEquals(1, patterns.size)
        assertEquals(13, patterns.first().cells.size)
    }

    @Test
    fun cartonCompleto_hasOnePatternOfTwentyFiveCells() {
        val patterns = GameMode.CARTON_COMPLETO.patterns
        assertEquals(1, patterns.size)
        assertEquals(25, patterns.first().cells.size)
    }

    @Test
    fun l_containsColumnBAndRowFive() {
        val cells = GameMode.L.patterns.first().cells
        BingoLetter.entries.forEach { letter ->
            assertTrue(cells.contains(GridPosition(letter, 5)))
        }
        (1..5).forEach { row ->
            assertTrue(cells.contains(GridPosition(BingoLetter.B, row)))
        }
    }

    @Test
    fun i_containsRowOneColumnNAndRowFive() {
        val cells = GameMode.I.patterns.first().cells
        BingoLetter.entries.forEach { letter ->
            assertTrue(cells.contains(GridPosition(letter, 1)))
            assertTrue(cells.contains(GridPosition(letter, 5)))
        }
        (1..5).forEach { row ->
            assertTrue(cells.contains(GridPosition(BingoLetter.N, row)))
        }
    }
}
