package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.repository.ActiveGame
import com.sergiodev.bingo.domain.repository.ActiveGameRepository

/** In-memory [ActiveGameRepository] for holder tests; counts calls so tests can assert persistence. */
class InMemoryActiveGameRepository(var stored: ActiveGame? = null) : ActiveGameRepository {
    var saveCalls = 0
        private set
    var clearCalls = 0
        private set

    override suspend fun load(): ActiveGame? = stored

    override suspend fun save(game: ActiveGame) {
        saveCalls++
        stored = game
    }

    override suspend fun clear() {
        clearCalls++
        stored = null
    }
}
