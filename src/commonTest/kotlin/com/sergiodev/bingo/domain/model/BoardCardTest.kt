package com.sergiodev.bingo.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class BoardCardTest {

    // Column-major order: B1-5, I1-5, N1,N2,N4,N5 (FREE at N row3 omitted), G1-5, O1-5
    private val numbers = listOf(
        1, 2, 3, 4, 5, // B
        16, 17, 18, 19, 20, // I
        31, 32, 34, 35, // N (FREE row 3 omitted)
        46, 47, 48, 49, 50, // G
        61, 62, 63, 64, 65, // O
    )
    private val board = BoardCard(id = 1L, identifier = "Casa1", numbers = numbers)

    @Test
    fun numberAt_freeCellIsNull() {
        assertNull(board.numberAt(GridPosition(BingoLetter.N, 3)))
    }

    @Test
    fun numberAt_bColumnFirstRow() {
        assertEquals(1, board.numberAt(GridPosition(BingoLetter.B, 1)))
    }

    @Test
    fun numberAt_nColumnRow4SkipsFree() {
        // N row 1,2 = 31,32 ; row3 = FREE(null) ; row4,5 = 34,35
        assertEquals(34, board.numberAt(GridPosition(BingoLetter.N, 4)))
    }

    @Test
    fun numberAt_oColumnLastRow() {
        assertEquals(65, board.numberAt(GridPosition(BingoLetter.O, 5)))
    }
}
