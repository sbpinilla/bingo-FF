package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Two-pane shell: boards on the left (2/3), game setup and play on the right (1/3). */
@Composable
fun AppWindow() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                PlaceholderPane("Boards", Modifier.weight(2f).fillMaxHeight())
                PlaceholderPane("Game", Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun PlaceholderPane(title: String, modifier: Modifier) {
    Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(title)
    }
}
