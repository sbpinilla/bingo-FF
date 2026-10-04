package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Shell top bar with the overflow menu holding the board import and export actions. */
@Composable
fun AppMenu(
    onExportToFile: () -> Unit,
    onCopyToClipboard: () -> Unit,
    onImportFromFile: () -> Unit,
    onImportPasted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    fun item(label: String, action: () -> Unit): @Composable () -> Unit = {
        DropdownMenuItem(
            text = { Text(label) },
            onClick = {
                expanded = false
                action()
            },
        )
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { expanded = true }) { Text("Menú") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            item("Exportar cartones a archivo", onExportToFile)()
            item("Copiar cartones como JSON", onCopyToClipboard)()
            item("Importar desde archivo", onImportFromFile)()
            item("Importar pegando JSON", onImportPasted)()
        }
    }
}
