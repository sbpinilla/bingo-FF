package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.presentation.GameSessionHolder

private val Success = Color(0xFF2E7D32)
private val OnSuccess = Color(0xFFFFFFFF)

/** Right pane while no game is active: pick the win mode and start. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupPane(session: GameSessionHolder, modifier: Modifier = Modifier) {
    val state by session.setup.collectAsState()
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nuevo juego", style = MaterialTheme.typography.titleLarge)
        if (!state.hasBoards) {
            Text("Registra al menos un cartón para poder jugar")
        }
        Text("Selecciona el modo de juego")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.availableModes.forEach { mode ->
                FilterChip(
                    selected = state.selectedMode == mode,
                    onClick = { session.selectMode(mode) },
                    label = { Text(mode.label(), maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Success,
                        selectedLabelColor = OnSuccess,
                    ),
                )
            }
        }
        Button(
            onClick = { state.selectedMode?.let(session::start) },
            enabled = state.canStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Jugar")
        }
    }
}

internal fun GameMode.label(): String = when (this) {
    GameMode.COLUMNA -> "Columna"
    GameMode.O -> "O"
    GameMode.L -> "L"
    GameMode.I -> "I"
    GameMode.CARTON_COMPLETO -> "Completo"
}
