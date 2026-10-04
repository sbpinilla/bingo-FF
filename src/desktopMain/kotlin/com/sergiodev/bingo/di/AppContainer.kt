package com.sergiodev.bingo.di

import com.sergiodev.bingo.data.file.AppDirs
import com.sergiodev.bingo.data.local.BingoDatabase
import com.sergiodev.bingo.data.local.RoomBoardRepository
import com.sergiodev.bingo.data.local.buildBingoDatabase
import com.sergiodev.bingo.domain.repository.BoardRepository
import com.sergiodev.bingo.presentation.BoardsState
import com.sergiodev.bingo.platform.AwtClipboard
import com.sergiodev.bingo.platform.AwtFileDialogs
import com.sergiodev.bingo.platform.JvmTextFiles
import com.sergiodev.bingo.presentation.CreateBoardHolder
import com.sergiodev.bingo.presentation.ImportExportHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.awt.Frame

/** Manual composition root. State holders run on the Swing/Main dispatcher. */
class AppContainer(dirs: AppDirs = AppDirs.default()) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val database: BingoDatabase = buildBingoDatabase(dirs.databaseFile)

    val boardRepository: BoardRepository = RoomBoardRepository(database)
    val boardsState = BoardsState(boardRepository, scope)

    /** Window that owns native file dialogs; set by the window once it exists. */
    var dialogOwner: Frame? = null

    val importExportHolder = ImportExportHolder(
        repository = boardRepository,
        dialogs = AwtFileDialogs { dialogOwner },
        files = JvmTextFiles(),
        clipboard = AwtClipboard(),
        scope = scope,
    )

    fun newCreateBoardHolder() = CreateBoardHolder(boardRepository, scope)

    fun close() {
        scope.cancel()
        database.close()
    }
}
