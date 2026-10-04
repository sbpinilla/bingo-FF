package com.sergiodev.bingo.domain.model

/**
 * A registered bingo board.
 *
 * [numbers] holds exactly 24 numbers in fixed column-major order:
 * B1-B5, I1-I5, N1,N2,N4,N5 (FREE at N row 3 omitted), G1-G5, O1-O5.
 */
data class BoardCard(
    val id: Long,
    val identifier: String,
    val numbers: List<Int>,
) {
    /**
     * Returns the number placed at [pos], or `null` exactly for the FREE cell
     * (N column, row 3).
     */
    fun numberAt(pos: GridPosition): Int? {
        if (pos.column == BingoLetter.N && pos.row == 3) return null

        val columnOrder = listOf(BingoLetter.B, BingoLetter.I, BingoLetter.N, BingoLetter.G, BingoLetter.O)
        val columnIndex = columnOrder.indexOf(pos.column)
        var offset = columnIndex * 5

        // N column has only 4 stored numbers (FREE at row 3 is omitted).
        if (pos.column == BingoLetter.N) {
            val rowWithinN = if (pos.row > 3) pos.row - 1 else pos.row
            offset = (columnOrder.indexOf(BingoLetter.N) * 5) + (rowWithinN - 1)
            return numbers.getOrNull(offset)
        }

        // Columns after N are shifted back by one because N stores only 4 numbers.
        if (columnIndex > columnOrder.indexOf(BingoLetter.N)) {
            offset -= 1
        }

        return numbers.getOrNull(offset + (pos.row - 1))
    }
}
