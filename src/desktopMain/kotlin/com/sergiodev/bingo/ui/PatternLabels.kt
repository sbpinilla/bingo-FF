package com.sergiodev.bingo.ui

import androidx.compose.runtime.Composable

/** Localized display text for a win pattern id. */
@Composable
internal fun patternLabel(patternId: String): String = patternUiText(patternId).resolve()
