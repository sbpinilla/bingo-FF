package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.platform.Clipboard
import com.sergiodev.bingo.platform.FileDialogs
import com.sergiodev.bingo.platform.TextFiles

/** Scripted dialogs: [openResult] and [saveResult] are what the "user" picks; null means cancel. */
class FakeFileDialogs(
    var openResult: String? = null,
    var saveResult: String? = null,
) : FileDialogs {
    var openCalls = 0
        private set
    var saveCalls = 0
        private set
    var lastSuggestedName: String? = null
        private set

    override suspend fun pickOpen(): String? {
        openCalls++
        return openResult
    }

    override suspend fun pickSave(suggestedName: String): String? {
        saveCalls++
        lastSuggestedName = suggestedName
        return saveResult
    }
}

/** In-memory file system keyed by path. A path in [unreadable] fails on read, [failWrites] fails every write. */
class FakeTextFiles : TextFiles {
    val files = mutableMapOf<String, String>()
    val unreadable = mutableSetOf<String>()
    var failWrites = false
    var writeCalls = 0
        private set

    override suspend fun read(path: String): String {
        if (path in unreadable) error("cannot read $path")
        return files[path] ?: error("no such file $path")
    }

    override suspend fun write(path: String, text: String) {
        writeCalls++
        if (failWrites) error("disk full")
        files[path] = text
    }
}

class FakeClipboard : Clipboard {
    var copied: String? = null
        private set

    override fun copy(text: String) {
        copied = text
    }
}
