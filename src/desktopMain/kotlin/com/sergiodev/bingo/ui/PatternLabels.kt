package com.sergiodev.bingo.ui

/** Display text for a win pattern id. Hard-coded Spanish until the localization unit. */
internal fun patternLabel(patternId: String): String = when {
    patternId.startsWith("COLUMN_") -> "Columna ${patternId.removePrefix("COLUMN_")}"
    patternId == "FULL_CARD" -> "Cartón completo"
    else -> patternId
}
