package com.sergiodev.bingo.i18n

import com.sergiodev.bingo.resources.Res
import com.sergiodev.bingo.resources.game_play_submit_call_button
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/** Drives the real resource lookup from the JVM default locale, as the desktop app does at launch. */
class LocaleSelectionTest {
    private val original = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(original)

    private fun submitLabel(locale: Locale): String {
        Locale.setDefault(locale)
        return runBlocking { getString(getSystemResourceEnvironment(), Res.string.game_play_submit_call_button) }
    }

    @Test
    fun english_language_gives_english_text() {
        assertEquals("Call number", submitLabel(Locale.forLanguageTag("en-US")))
        assertEquals("Call number", submitLabel(Locale.forLanguageTag("en-GB")))
    }

    @Test
    fun unsupported_language_gives_spanish_text() {
        assertEquals("Cantar número", submitLabel(Locale.forLanguageTag("fr-FR")))
        assertEquals("Cantar número", submitLabel(Locale.forLanguageTag("es-AR")))
    }
}
