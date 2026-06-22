package com.liman.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Favorite
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
    const val MOOD_ENTRY = "mood_entry"
    const val JOURNAL_EDITOR = "journal_editor"
    const val ADD_CONTACT = "add_contact"

    const val EDIT_CONTACT = "edit_contact"
    fun editContact(id: String) = "$EDIT_CONTACT/$id"
    const val EDIT_CONTACT_ROUTE = "$EDIT_CONTACT/{$CONTACT_ARG}"

    const val CONTACT = "contact"
    const val CONTACT_ARG = "contactId"
    fun contact(id: String) = "$CONTACT/$id"
    const val CONTACT_ROUTE = "$CONTACT/{$CONTACT_ARG}"

    const val JOURNAL_VIEWER = "journal_viewer"
    const val JOURNAL_ARG = "journalId"
    fun journal(id: String) = "$JOURNAL_VIEWER/$id"
    const val JOURNAL_VIEWER_ROUTE = "$JOURNAL_VIEWER/{$JOURNAL_ARG}"

    const val MEMORY = "memory"
    const val MEMORY_CONTACT_ARG = "memContactId"
    const val MEMORY_ARG = "memoryId"
    fun memory(contactId: String, memoryId: String) = "$MEMORY/$contactId/$memoryId"
    const val MEMORY_ROUTE = "$MEMORY/{$MEMORY_CONTACT_ARG}/{$MEMORY_ARG}"

    // Ben sekmesi araçları
    const val TOOL_BREATHING = "tool_breathing"
    const val TOOL_THOUGHT = "tool_thought"
    const val TOOL_GRATITUDE = "tool_gratitude"
    const val TOOL_CAPSULE = "tool_capsule"
    const val TOOL_CALENDAR = "tool_calendar"
}

/** Alt navigasyon sekmeleri. */
enum class Tab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
    val showLockBadge: Boolean = false,
) {
    TODAY("tab_today", "Bugün", Icons.Rounded.WbSunny, Icons.Outlined.WbSunny),
    ME("tab_me", "Ben", Icons.Rounded.Spa, Icons.Outlined.Spa, showLockBadge = true),
    BONDS("tab_bonds", "Bağlar", Icons.Rounded.Favorite, Icons.Outlined.Favorite),
    INSIGHT("tab_insight", "İçgörü", Icons.Rounded.Insights, Icons.Outlined.Insights),
}
