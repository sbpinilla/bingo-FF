package com.sergiodev.bingo.data.file

import com.sergiodev.bingo.domain.repository.ThemeMode
import com.sergiodev.bingo.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.nio.file.Path

/** On-disk shape of `theme.json`. */
@Serializable
internal data class ThemeDto(val version: Int = CURRENT_VERSION, val mode: ThemeMode) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * [ThemeRepository] backed by `theme.json`. A missing, corrupt, unknown-version or unrecognized
 * file means [ThemeMode.SYSTEM]. The file is read lazily on first collection.
 */
class FileThemeRepository(file: Path) : ThemeRepository {
    private val store = JsonFileStore(file, ThemeDto.serializer())
    private val current = MutableStateFlow<ThemeMode?>(null)

    override val themeMode: Flow<ThemeMode> = flow {
        if (current.value == null) {
            val stored = store.load()?.takeIf { it.version == ThemeDto.CURRENT_VERSION }?.mode ?: ThemeMode.SYSTEM
            // A set() that raced the read wins: only fill in when still unset.
            current.compareAndSet(null, stored)
        }
        emitAll(current.filterNotNull())
    }

    override suspend fun set(mode: ThemeMode) {
        current.value = mode
        store.save(ThemeDto(mode = mode))
    }
}
