package com.sergiodev.bingo.data.json

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Decodes a file written in the exact byte shape Gson produces on Android. */
class AndroidExportFixtureTest {

    private fun fixture(): String {
        val stream = assertNotNull(javaClass.getResourceAsStream("/android-export-sample.json"))
        return stream.use { it.readBytes().decodeToString() }
    }

    @Test
    fun androidExportFile_decodesWithEscapedCharacters() {
        val boards = BoardJsonCodec.decode(fixture())

        assertEquals(listOf(1L, 12L), boards.map { it.id })
        assertEquals("A=1 <vip>", boards[1].identifier)
        assertEquals(24, boards[0].numbers.size)
    }

    @Test
    fun androidExportFile_reencodesToEquivalentBoards() {
        val boards = BoardJsonCodec.decode(fixture())

        assertEquals(boards, BoardJsonCodec.decode(BoardJsonCodec.encode(boards)))
    }
}
