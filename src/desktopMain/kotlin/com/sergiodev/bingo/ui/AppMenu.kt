package com.sergiodev.bingo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Column
import com.sergiodev.bingo.domain.repository.ThemeMode

/** Shell top bar with the overflow menu holding the board import and export actions. */
@Composable
fun AppMenu(
    onExportToFile: () -> Unit,
    onCopyToClipboard: () -> Unit,
    onImportFromFile: () -> Unit,
    onImportPasted: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
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
            HorizontalDivider()
            ThemeOptions(themeMode, onThemeChange)
        }
    }
}

/** Radio group for the theme mode; applies on click and leaves the menu open so the change is visible. */
@Composable
private fun ThemeOptions(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(Modifier.selectableGroup()) {
        Text("Tema", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge)
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = mode == selected, onClick = { onSelect(mode) }, role = Role.RadioButton)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = mode == selected, onClick = null)
                Spacer(Modifier.width(8.dp))
                Text(mode.label())
            }
        }
    }
}

internal fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Claro"
    ThemeMode.DARK -> "Oscuro"
    ThemeMode.SYSTEM -> "Sistema"
}
