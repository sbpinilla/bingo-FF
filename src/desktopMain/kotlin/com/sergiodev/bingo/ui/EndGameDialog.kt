package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun EndGameDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.game_play_end_game_dialog_title)) },
        text = { Text(stringResource(Res.string.game_play_end_game_dialog_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.game_play_end_game_confirm_button)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}
