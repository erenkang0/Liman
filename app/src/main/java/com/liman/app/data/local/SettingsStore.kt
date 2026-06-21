package com.liman.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liman.app.data.model.AppSettings
import com.liman.app.data.model.AutoLock
import com.liman.app.data.model.Gender
import com.liman.app.data.model.LockLocation
import com.liman.app.data.model.ThemeMode
import com.liman.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "liman_settings")

/**
 * Uygulama ayarlarını cihazda (DataStore) saklar. Tüm bilgiler yalnızca
 * cihazda tutulur; yedeklemeden hariç bırakılır (bkz. backup_rules.xml).
 *
 * Bildirim tercihleri demo amacıyla bellekte (varsayılan) tutulur.
 */
class SettingsStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { p -> p.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = prefs.toSettings()
            prefs.write(transform(current))
        }
    }

    private fun Preferences.toSettings(): AppSettings {
        val birthEpoch = this[Keys.birthDate]
        return AppSettings(
            profile = UserProfile(
                name = this[Keys.name].orEmpty(),
                birthDate = birthEpoch?.let { LocalDate.ofEpochDay(it) },
                gender = this[Keys.gender]?.let { runCatching { Gender.valueOf(it) }.getOrNull() },
            ),
            onboarded = this[Keys.onboarded] ?: false,
            themeMode = this[Keys.themeMode]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = this[Keys.dynamicColor] ?: false,
            lockEnabled = this[Keys.lockEnabled] ?: true,
            lockLocation = this[Keys.lockLocation]?.let { runCatching { LockLocation.valueOf(it) }.getOrNull() }
                ?: LockLocation.INNER_WORLD_ONLY,
            biometricEnabled = this[Keys.biometricEnabled] ?: true,
            pin = this[Keys.pin],
            autoLock = this[Keys.autoLock]?.let { runCatching { AutoLock.valueOf(it) }.getOrNull() }
                ?: AutoLock.FIVE_MIN,
        )
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.write(s: AppSettings) {
        this[Keys.onboarded] = s.onboarded
        this[Keys.name] = s.profile.name
        s.profile.birthDate?.let { this[Keys.birthDate] = it.toEpochDay() }
        s.profile.gender?.let { this[Keys.gender] = it.name }
        this[Keys.themeMode] = s.themeMode.name
        this[Keys.dynamicColor] = s.dynamicColor
        this[Keys.lockEnabled] = s.lockEnabled
        this[Keys.lockLocation] = s.lockLocation.name
        this[Keys.biometricEnabled] = s.biometricEnabled
        s.pin?.let { this[Keys.pin] = it }
        this[Keys.autoLock] = s.autoLock.name
    }

    private object Keys {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("profile_name")
        val birthDate = longPreferencesKey("profile_birth")
        val gender = stringPreferencesKey("profile_gender")
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val lockEnabled = booleanPreferencesKey("lock_enabled")
        val lockLocation = stringPreferencesKey("lock_location")
        val biometricEnabled = booleanPreferencesKey("biometric_enabled")
        val pin = stringPreferencesKey("lock_pin")
        val autoLock = stringPreferencesKey("auto_lock")
    }
}
