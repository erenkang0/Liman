package com.liman.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liman.app.data.crypto.CryptoManager
import com.liman.app.data.model.Contact
import com.liman.app.data.model.JournalEntry
import com.liman.app.data.model.MoodEntry
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.storeDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "liman_store")

/**
 * Kullanıcı verisini (kişiler/defterler, ruh hali, günlük) cihazda **kalıcı** ve
 * **şifreli** saklar. Tüm anlık görüntü JSON'a serialize edilip [CryptoManager]
 * ile şifrelenerek DataStore'da tutulur; uygulama kapanıp açıldığında geri yüklenir.
 *
 * Not: Günlük gövdeleri zaten ayrıca şifrelidir; burada tüm dosya bir kez daha
 * şifrelenir (katmanlı koruma).
 */
class LimanStore(
    private val context: Context,
    private val crypto: CryptoManager,
) {
    @Serializable
    data class Snapshot(
        val moods: List<MoodEntry> = emptyList(),
        /** Günlük gövdeleri burada şifreli (depolama) biçiminde tutulur. */
        val journals: List<JournalEntry> = emptyList(),
        val contacts: List<Contact> = emptyList(),
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun load(): Snapshot {
        val raw = context.storeDataStore.data.first()[Key.blob] ?: return Snapshot()
        return runCatching {
            json.decodeFromString(Snapshot.serializer(), crypto.decrypt(raw))
        }.getOrDefault(Snapshot())
    }

    suspend fun save(snapshot: Snapshot) {
        val plain = json.encodeToString(Snapshot.serializer(), snapshot)
        val encrypted = crypto.encrypt(plain)
        context.storeDataStore.edit { it[Key.blob] = encrypted }
    }

    private object Key {
        val blob = stringPreferencesKey("liman_snapshot")
    }
}
