package com.sergiodev.bingo.data.json

import com.sergiodev.bingo.domain.model.BoardCard
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BoardJsonCodecTest {

    private val board = BoardCard(
        id = 7,
        identifier = "Casa1",
        numbers = listOf(
            3, 7, 12, 14, 15,
            16, 17, 18, 19, 20,
            31, 32, 34, 35,
            46, 47, 48, 49, 50,
            61, 62, 63, 64, 65,
        ),
    )

    private val other = BoardCard(id = 9, identifier = "Casa2", numbers = List(24) { it + 1 })

    @Test
    fun encodeThenDecode_roundTripsIdIdentifierAndNumbers() {
        assertEquals(listOf(board, other), BoardJsonCodec.decode(BoardJsonCodec.encode(listOf(board, other))))
    }

    @Test
    fun encode_emptyList_producesValidEmptyArray() {
        val json = BoardJsonCodec.encode(emptyList())

        assertEquals("[]", json)
        assertEquals(emptyList(), BoardJsonCodec.decode(json))
    }

    @Test
    fun encode_isABareArrayWithoutWrapperOrVersion() {
        val json = BoardJsonCodec.encode(listOf(board))

        assertEquals(
            """[{"id":7,"identifier":"Casa1","numbers":[3,7,12,14,15,16,17,18,19,20,31,32,34,35,46,47,48,49,50,61,62,63,64,65]}]""",
            json,
        )
    }

    @Test
    fun decode_malformedJson_throws() {
        assertFailsWith<SerializationException> { BoardJsonCodec.decode("not valid json {{{") }
    }

    @Test
    fun decode_missingIdentifier_throws() {
        assertFailsWith<SerializationException> {
            BoardJsonCodec.decode("""[{"id": 1, "numbers": [1,2,3]}]""")
        }
    }

    @Test
    fun decode_oneEntryMissingNumbers_rejectsTheWholePayload() {
        val json = """
            [{"id":1,"identifier":"A","numbers":[1,2,3]},
             {"id":2,"identifier":"B"},
             {"id":3,"identifier":"C","numbers":[4,5,6]}]
        """.trimIndent()

        assertFailsWith<SerializationException> { BoardJsonCodec.decode(json) }
    }

    @Test
    fun decode_nonArrayRoot_throws() {
        assertFailsWith<SerializationException> {
            BoardJsonCodec.decode("""{"id":1,"identifier":"A","numbers":[1]}""")
        }
    }

    @Test
    fun decode_wrongTypedField_throws() {
        assertFailsWith<SerializationException> {
            BoardJsonCodec.decode("""[{"id":1,"identifier":"A","numbers":["x"]}]""")
        }
        assertFailsWith<SerializationException> {
            BoardJsonCodec.decode("""[{"id":1,"identifier":5,"numbers":[1]}]""")
        }
    }

    @Test
    fun decode_ignoresUnknownKeys() {
        val decoded = BoardJsonCodec.decode("""[{"id":1,"identifier":"A","numbers":[1,2],"extra":true}]""")

        assertEquals(listOf(BoardCard(1, "A", listOf(1, 2))), decoded)
    }

    @Test
    fun decode_androidGsonExport_isAccepted() {
        // Exactly the shape Gson 2.x emits for List<BoardExportDto>: compact, property order
        // id/identifier/numbers, HTML-safe escapes (= is '=') for special characters.
        val json = """[{"id":1,"identifier":"Casa1","numbers":[3,7,12,14,15,16,17,18,19,20,31,32,34,35,46,47,48,49,50,61,62,63,64,65]},""" +
            """{"id":12,"identifier":"A=1 <vip>","numbers":[1,2,3]}]"""

        val decoded = BoardJsonCodec.decode(json)

        assertEquals(2, decoded.size)
        assertEquals(BoardCard(1, "Casa1", board.numbers), decoded[0])
        assertEquals(BoardCard(12, "A=1 <vip>", listOf(1, 2, 3)), decoded[1])
    }
}
