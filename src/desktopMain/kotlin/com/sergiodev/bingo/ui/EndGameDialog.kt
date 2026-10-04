package com.sergiodev.bingo.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun EndGameDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Terminar juego?") },
        text = { Text("Se perderán los números cantados y los bingos anunciados de esta partida.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Terminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
