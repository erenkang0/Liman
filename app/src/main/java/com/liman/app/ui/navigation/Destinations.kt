package com.liman.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** Üst düzey rota adları. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"

    const val SETTINGS = "settings"
    const val REMINDERS = "reminders"
    const val CALM = "calm"
    const val SEARCH = "search"
    const val WEEKLY_REPORT = "weekly_report"
    const val MOOD_ENTRY = "mood_entry"
    const val JOURNAL_EDITOR = "journal_editor"
    const val ADD_CONTACT = "add_contact"

    const val CONTACT = "contact"
    const val CONTACT_ARG = "contactId"
    fun contact(id: String) = "$CONTACT/$id"
    const val CONTACT_ROUTE = "$CONTACT/{$CONTACT_ARG}"

    const val JOURNAL_VIEWER = "journal_viewer"
    const val JOURNAL_ARG = "journalId"
    fun journal(id: String) = "$JOURNAL_VIEWER/$id"
    const val JOURNAL_VIEWER_ROUTE = "$JOURNAL_VIEWER/{$JOURNAL_ARG}"

    // Kişiye özel defterdeki tek bir kayıt
    const val NOTE = "note"
    const val NOTE_CONTACT_ARG = "noteContactId"
    const val NOTE_ARG = "noteId"
    fun note(contactId: String, noteId: String) = "$NOTE/$contactId/$noteId"
    const val NOTE_ROUTE = "$NOTE/{$NOTE_CONTACT_ARG}/{$NOTE_ARG}"

    // Ben sekmesi araçları
    const val TOOL_BREATHING = "tool_breathing"
    const val TOOL_THOUGHT = "tool_thought"
    const val TOOL_GRATITUDE = "tool_gratitude"
    const val TOOL_CAPSULE = "tool_capsule"
    const val TOOL_CALENDAR = "tool_calendar"
}

/** İki "dünya": Psikolog Defteri (varsayılan) ve Liman (zindelik günlüğü). */
enum class World(val label: String) {
    DEFTER("Defter"),
    LIMAN("Liman"),
}

/**
 * Alt navigasyon sekmeleri — her sekme bir [World]'e aittir. Bottom bar yalnızca
 * o anki dünyanın sekmelerini gösterir.
 */
enum class Tab(
    val world: World,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val showLockBadge: Boolean = false,
) {
    // Defter dünyası
    PEOPLE(World.DEFTER, "Kişiler", Icons.Rounded.Groups, Icons.Outlined.Groups),
    SUMMARY(World.DEFTER, "Özet", Icons.Rounded.AutoStories, Icons.Outlined.AutoStories),

    // Liman dünyası
    TODAY(World.LIMAN, "Bugün", Icons.Rounded.WbSunny, Icons.Outlined.WbSunny),
    ME(World.LIMAN, "Ben", Icons.Rounded.Spa, Icons.Outlined.Spa, showLockBadge = true),
    INSIGHT(World.LIMAN, "İçgörü", Icons.Rounded.Insights, Icons.Outlined.Insights);

    companion object {
        fun firstOf(world: World): Tab = entries.first { it.world == world }
        fun of(world: World): List<Tab> = entries.filter { it.world == world }
    }
}
