package com.sergiodev.bingo.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.nio.file.Files
import java.nio.file.Path

@Database(entities = [BoardEntity::class], version = 1, exportSchema = true)
@TypeConverters(IntListConverter::class)
abstract class BingoDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
}

/** Opens (creating parent directories) the on-disk database at [file] with the bundled SQLite driver. */
fun buildBingoDatabase(file: Path): BingoDatabase {
    file.parent?.let { Files.createDirectories(it) }
    return Room.databaseBuilder<BingoDatabase>(name = file.toString())
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
