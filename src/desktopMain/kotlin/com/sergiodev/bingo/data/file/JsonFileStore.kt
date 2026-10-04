package com.sergiodev.bingo.data.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Stores one value as a JSON file. Writes go to a sibling temp file that is then moved over the
 * target, so a crash never leaves a half-written file. Missing, unreadable or corrupt files load as `null`.
 */
class JsonFileStore<T : Any>(
    private val file: Path,
    private val serializer: KSerializer<T>,
    private val json: Json = DefaultJson,
) {
    suspend fun load(): T? = withContext(Dispatchers.IO) {
        try {
            if (!Files.exists(file)) null else json.decodeFromString(serializer, Files.readString(file))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun save(value: T): Unit = withContext(Dispatchers.IO) {
        file.parent?.let { Files.createDirectories(it) }
        val temp = file.resolveSibling(file.fileName.toString() + ".tmp")
        Files.writeString(temp, json.encodeToString(serializer, value))
        try {
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
        }
        Unit
    }

    suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        Files.deleteIfExists(file)
        Unit
    }

    companion object {
        val DefaultJson = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
