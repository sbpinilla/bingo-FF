package com.sergiodev.bingo.ui

import com.sergiodev.bingo.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.sergiodev.bingo.domain.repository.ThemeMode

/**
 * Native window menu bar (system menu bar on macOS). It replaces the old in-content overflow
 * button: the shell keeps all its vertical space for the boards grid, and import/export and
 * the theme radio group sit where desktop users expect them.
 */
@Composable
fun FrameWindowScope.AppMenuBar(
    onExportToFile: () -> Unit,
    onCopyToClipboard: () -> Unit,
    onImportFromFile: () -> Unit,
    onImportPasted: () -> Unit,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    MenuBar {
        Menu(stringResource(Res.string.menu_label)) {
            Item(stringResource(Res.string.menu_export_file), onClick = onExportToFile)
            Item(stringResource(Res.string.menu_export_clipboard), onClick = onCopyToClipboard)
            Separator()
            Item(stringResource(Res.string.menu_import_file), onClick = onImportFromFile)
            Item(stringResource(Res.string.menu_import_paste), onClick = onImportPasted)
        }
        Menu(stringResource(Res.string.theme_title)) {
            ThemeMode.entries.forEach { mode ->
                RadioButtonItem(
                    text = mode.uiText().resolve(),
                    selected = mode == themeMode,
                    onClick = { onThemeChange(mode) },
                )
            }
        }
    }
}
