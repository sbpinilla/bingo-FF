package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

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
import com.sergiodev.bingo.presentation.MAX_NUMBER_LENGTH
import com.sergiodev.bingo.presentation.flatFieldIndex
import com.sergiodev.bingo.presentation.sanitizeNumberInput

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
        title = { Text(stringResource(Res.string.create_board_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(
                    value = state.identifier,
                    onValueChange = holder::onIdentifierChange,
                    label = { Text(stringResource(Res.string.create_board_identifier_label)) },
                    singleLine = true,
                    isError = state.identifierError != null,
                    supportingText = { state.identifierError?.let { Text(it.uiText().resolve()) } },
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
                                    val digits = sanitizeNumberInput(raw)
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
        confirmButton = { TextButton(onClick = holder::onSubmit) { Text(stringResource(Res.string.create_board_save_button)) } },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(Res.string.common_cancel)) } },
    )
}
