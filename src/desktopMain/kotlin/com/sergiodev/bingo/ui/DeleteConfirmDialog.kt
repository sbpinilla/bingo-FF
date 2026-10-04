package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sergiodev.bingo.domain.model.BoardCard

@Composable
fun DeleteConfirmDialog(board: BoardCard, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.board_list_delete_dialog_title)) },
        text = { Text(stringResource(Res.string.board_list_delete_dialog_message, board.id, board.identifier)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.board_list_delete_button)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) } },
    )
}
