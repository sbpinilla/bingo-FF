package com.sergiodev.bingo.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Native AWT file pickers. The modal call runs on the Swing dispatcher ([Dispatchers.Main]).
 * `FilenameFilter` is ignored on Windows, so the `*.json` hint goes through `setFile` and the
 * imported content is validated by the holder instead of trusting the extension.
 */
class AwtFileDialogs(private val owner: () -> Frame? = { null }) : FileDialogs {

    override suspend fun pickOpen(): String? = withContext(Dispatchers.Main) {
        show(FileDialog.LOAD, "Importar cartones", JSON_PATTERN)
    }

    override suspend fun pickSave(suggestedName: String): String? = withContext(Dispatchers.Main) {
        show(FileDialog.SAVE, "Exportar cartones", suggestedName)
    }

    private fun show(mode: Int, title: String, file: String): String? {
        val dialog = FileDialog(owner(), title, mode)
        dialog.file = file
        if (mode == FileDialog.LOAD) dialog.setFilenameFilter { _, name -> name.endsWith(JSON_EXTENSION, ignoreCase = true) }
        try {
            dialog.isVisible = true
            val name = dialog.file ?: return null
            return File(dialog.directory.orEmpty(), name).path
        } finally {
            dialog.dispose()
        }
    }

    private companion object {
        const val JSON_EXTENSION = ".json"
        const val JSON_PATTERN = "*.json"
    }
}
