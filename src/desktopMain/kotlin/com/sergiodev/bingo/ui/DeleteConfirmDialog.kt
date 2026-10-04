package com.sergiodev.bingo.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sergiodev.bingo.domain.model.BoardCard

@Composable
fun DeleteConfirmDialog(board: BoardCard, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar cartón") },
        text = { Text("¿Eliminar el cartón #${board.id} (${board.identifier})? Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
