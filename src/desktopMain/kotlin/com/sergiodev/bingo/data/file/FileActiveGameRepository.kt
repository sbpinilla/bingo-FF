package com.sergiodev.bingo.data.file

import com.sergiodev.bingo.domain.model.BingoLetter
import com.sergiodev.bingo.domain.model.GameMode
import com.sergiodev.bingo.domain.repository.ActiveGame
import com.sergiodev.bingo.domain.repository.ActiveGameRepository
import kotlinx.serialization.Serializable
import java.nio.file.Path

/** On-disk shape of `active-game.json`. Winners are not stored; they are rebuilt by replay. */
@Serializable
internal data class ActiveGameDto(
    val version: Int = ActiveGameDto.CURRENT_VERSION,
    val mode: GameMode,
    val calledNumbers: List<Int>,
    val dismissedLetters: List<BingoLetter>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/** [ActiveGameRepository] backed by `active-game.json`. Corrupt or unknown-version files mean no game. */
class FileActiveGameRepository(file: Path) : ActiveGameRepository {
    private val store = JsonFileStore(file, ActiveGameDto.serializer())

    override suspend fun load(): ActiveGame? =
        store.load()
            ?.takeIf { it.version == ActiveGameDto.CURRENT_VERSION }
            ?.let { ActiveGame(it.mode, it.calledNumbers, it.dismissedLetters.toSet()) }

    override suspend fun save(game: ActiveGame) {
        store.save(
            ActiveGameDto(
                mode = game.mode,
                calledNumbers = game.calledNumbers,
                dismissedLetters = game.dismissedLetters.sorted(),
            ),
        )
    }

    override suspend fun clear() {
        store.clear()
    }
}
