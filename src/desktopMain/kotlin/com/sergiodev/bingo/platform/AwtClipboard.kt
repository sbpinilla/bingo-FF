package com.sergiodev.bingo.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/** System clipboard through AWT. */
class AwtClipboard : Clipboard {
    override fun copy(text: String) {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }
}
