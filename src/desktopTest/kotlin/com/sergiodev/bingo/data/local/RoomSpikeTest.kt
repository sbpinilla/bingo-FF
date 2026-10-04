package com.sergiodev.bingo.data.local

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Spike (task 3.1): Room KMP + BundledSQLiteDriver round-trips a real DB file on the desktop JVM. */
class RoomSpikeTest {

    private fun open(path: String): BingoDatabase =
        Room.databaseBuilder<BingoDatabase>(name = path)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    @Test
    fun room_opens_and_roundtrips_on_desktop() = runTest {
        val dir = Files.createTempDirectory("bingoff-spike")
        val file = dir.resolve("spike.db")
        val numbers = List(24) { it + 1 }

        val first = open(file.toString())
        first.boardDao().insert(BoardEntity(identifier = "A1", numbers = numbers))
        first.close()
        assertTrue(Files.exists(file), "the database file must exist on disk")

        val second = open(file.toString())
        val stored = second.boardDao().observeAll().first()
        second.close()

        assertEquals(1, stored.size)
        assertEquals("A1", stored.single().identifier)
        assertEquals(numbers, stored.single().numbers)
        assertEquals(1L, stored.single().id)
    }
}
