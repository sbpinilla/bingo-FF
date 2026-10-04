package com.sergiodev.bingo.platform

/**
 * Native file pickers. Every function returns `null` when the user cancels.
 * Implementations own the threading (AWT dialogs must run on the Swing thread).
 */
interface FileDialogs {
    /** Shows an open dialog and returns the chosen path. */
    suspend fun pickOpen(): String?

    /** Shows a save dialog pre-filled with [suggestedName] and returns the chosen path. */
    suspend fun pickSave(suggestedName: String): String?
}

/** Whole-file text access for paths returned by [FileDialogs]. Both functions throw on I/O failure. */
interface TextFiles {
    suspend fun read(path: String): String

    suspend fun write(path: String, text: String)
}

/** System clipboard, text only. */
interface Clipboard {
    fun copy(text: String)
}
