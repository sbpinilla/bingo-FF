package com.sergiodev.bingo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sergiodev.bingo.domain.model.BoardCard

/**
 * [id] doubles as the app-assigned sequential board number (AUTOINCREMENT, so numbers
 * never repeat). [numbers] is stored as a comma-joined string (see [IntListConverter])
 * holding exactly 24 ints in column-major order, FREE cell omitted.
 */
@Entity(
    tableName = "board",
    indices = [Index(value = ["identifier"], unique = true)],
)
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identifier: String,
    @ColumnInfo(name = "numbers")
    val numbers: List<Int>,
) {
    fun toDomain(): BoardCard = BoardCard(id = id, identifier = identifier, numbers = numbers)
}
