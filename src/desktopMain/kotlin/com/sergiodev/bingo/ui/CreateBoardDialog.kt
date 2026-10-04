package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.presentation.CreateBoardErrorReason
import com.sergiodev.bingo.presentation.CreateBoardHolder

private const val MAX_NUMBER_LENGTH = 2
private const val FIELD_COUNT = 24
private const val FREE_ROW_INDEX = 2
private val FieldWidth = 64.dp

/** Modal dialog with the identifier and the 24 number fields grouped by letter. */
@Composable
fun CreateBoardDialog(holder: CreateBoardHolder, onClose: () -> Unit) {
    val state by holder.state.collectAsState()
    val focusRequesters = remember { List(FIELD_COUNT) { FocusRequester() } }

    LaunchedEffect(state.submitSuccess) {
        if (state.submitSuccess) onClose()
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Agregar cartón") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(
                    value = state.identifier,
                    onValueChange = holder::onIdentifierChange,
                    label = { Text("Identificador") },
                    singleLine = true,
                    isError = state.identifierError != null,
                    supportingText = { state.identifierError?.let { Text(it.message()) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                BingoLetter.entries.forEach { letter ->
                    Text(letter.name, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        val values = state.numbers.getValue(letter)
                        values.forEachIndexed { index, value ->
                            if (letter == BingoLetter.N && index == FREE_ROW_INDEX) {
                                Box(Modifier.width(FieldWidth), contentAlignment = Alignment.Center) { Text("★") }
                            }
                            val flat = flatFieldIndex(state.numbers, letter, index)
                            OutlinedTextField(
                                value = value,
                                onValueChange = { raw ->
                                    val digits = raw.filter(Char::isDigit).take(MAX_NUMBER_LENGTH)
                                    holder.onNumberChange(letter, index, digits)
                                    if (digits.length == MAX_NUMBER_LENGTH && flat < FIELD_COUNT - 1) {
                                        focusRequesters[flat + 1].requestFocus()
                                    }
                                },
                                singleLine = true,
                                isError = state.fieldErrors[letter]?.getOrNull(index) != null,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
                                modifier = Modifier.width(FieldWidth).focusRequester(focusRequesters[flat]),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = holder::onSubmit) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } },
    )
}

/** Flat position (0..23) of a field in reading order B, I, N (4), G, O. */
private fun flatFieldIndex(numbers: Map<BingoLetter, List<String>>, letter: BingoLetter, index: Int): Int =
    BingoLetter.entries.take(letter.ordinal).sumOf { numbers.getValue(it).size } + index

private fun CreateBoardErrorReason.message(): String = when (this) {
    CreateBoardErrorReason.BlankIdentifier -> "El identificador no puede estar vacío"
    CreateBoardErrorReason.DuplicateIdentifier -> "Ya existe un cartón con ese identificador"
}
