package com.sergiodev.bingo.data.json

import com.sergiodev.bingo.domain.model.BoardCard
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Wire shape of one board. Property names mirror Android's `BoardExportDto` so files
 * written by Gson on Android and by this codec are interchangeable.
 */
@Serializable
internal data class BoardDto(
    val id: Long,
    val identifier: String,
    val numbers: List<Int>,
)

/**
 * Encodes and decodes boards as a bare top-level JSON array (no wrapper object and no
 * version field), compatible with the Android export in both directions.
 */
object BoardJsonCodec {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(BoardDto.serializer())

    fun encode(boards: List<BoardCard>): String =
        json.encodeToString(serializer, boards.map { BoardDto(it.id, it.identifier, it.numbers) })

    /**
     * Decodes [text] into boards. Any structural or type error (invalid JSON, non-array
     * root, missing or wrong-typed field) throws [kotlinx.serialization.SerializationException]
     * and yields no partial result.
     */
    fun decode(text: String): List<BoardCard> =
        json.decodeFromString(serializer, text).map { BoardCard(it.id, it.identifier, it.numbers) }
}
