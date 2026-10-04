package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sergiodev.bingo.presentation.GameSessionHolder

/**
 * Right pane while a game is active. Placeholder for unit 6 (game-play): shows the mode and
 * call count and offers the confirmed end-game action that returns to setup.
 */
@Composable
fun PlayPane(session: GameSessionHolder, modifier: Modifier = Modifier) {
    val game by session.active.collectAsState()
    var confirmingEnd by remember { mutableStateOf(false) }
    val active = game ?: return
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Juego en curso", style = MaterialTheme.typography.titleLarge)
        Text("Modo: ${active.mode.label()} · Llamadas: ${active.calledNumbers.size}")
        Button(onClick = { confirmingEnd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Terminar juego")
        }
    }
    if (confirmingEnd) {
        EndGameDialog(
            onConfirm = {
                confirmingEnd = false
                session.end()
            },
            onDismiss = { confirmingEnd = false },
        )
    }
}
