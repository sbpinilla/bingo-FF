package com.sergiodev.bingo.presentation

import com.sergiodev.bingo.domain.model.BoardCard
import com.sergiodev.bingo.domain.repository.BoardRepository
import com.sergiodev.bingo.domain.repository.DuplicateIdentifierException
import com.sergiodev.bingo.domain.repository.ImportResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [BoardRepository] for holder tests. Ids ascend from 1. */
class FakeBoardRepository(seed: List<BoardCard> = emptyList()) : BoardRepository {
    val boards = MutableStateFlow(seed)
    var addCalls = 0
        private set
    var lastAdded: Pair<String, List<Int>>? = null
        private set
    var deletedIds = mutableListOf<Long>()
        private set

    override fun observeBoards(): Flow<List<BoardCard>> = boards

    override suspend fun addBoard(identifier: String, numbers: List<Int>): Result<Unit> {
        addCalls++
        if (boards.value.any { it.identifier == identifier }) {
            return Result.failure(DuplicateIdentifierException(identifier))
        }
        lastAdded = identifier to numbers
        val nextId = (boards.value.maxOfOrNull { it.id } ?: 0L) + 1
        boards.value = boards.value + BoardCard(nextId, identifier, numbers)
        return Result.success(Unit)
    }

    override suspend fun importBoards(boards: List<BoardCard>): ImportResult = ImportResult(0, 0)

    override suspend fun deleteBoard(id: Long) {
        deletedIds += id
        boards.value = boards.value.filterNot { it.id == id }
    }
}
