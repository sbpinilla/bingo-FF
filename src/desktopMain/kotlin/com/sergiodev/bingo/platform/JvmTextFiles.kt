package com.sergiodev.bingo.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

/** UTF-8 whole-file access on [Dispatchers.IO]. Throws `IOException` on failure. */
class JvmTextFiles : TextFiles {
    override suspend fun read(path: String): String = withContext(Dispatchers.IO) { Files.readString(Path.of(path)) }

    override suspend fun write(path: String, text: String) {
        withContext(Dispatchers.IO) { Files.writeString(Path.of(path), text) }
    }
}
