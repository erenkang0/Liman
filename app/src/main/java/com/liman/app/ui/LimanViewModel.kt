package com.liman.app.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liman.app.LimanApplication
import com.liman.app.data.model.AppSettings
import com.liman.app.data.model.AutoLock
import com.liman.app.data.model.Gender
import com.liman.app.data.model.LockLocation
import com.liman.app.data.model.ThemeMode
import com.liman.app.data.model.UserProfile
import com.liman.app.data.repository.LimanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Uygulamanın tek orkestratör ViewModel'i. Ayarları (DataStore) ve kilit
 * durumunu yönetir; veri akışlarını [LimanRepository]'den UI'a aktarır.
 */
class LimanViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as LimanApplication).container
    val repository: LimanRepository = container.repository
    private val settingsStore = container.settingsStore

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    /** Ayarlar DataStore'dan ilk kez yüklendi mi? (Splash'i tutmak için.) */
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    /** İç dünya kilidi açık mı? Oturum içi durum (kalıcı değil). */
    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var backgroundedAt: Long = 0L

    init {
        viewModelScope.launch {
            settingsStore.settings.first()
            _ready.value = true
        }
    }

    /* ----------------------------- Kilit -------------------------------- */
    fun unlock() {
        _unlocked.value = true
    }

    fun lock() {
        _unlocked.value = false
    }

    /** Uygulama arka plana geçtiğinde çağrılır. */
    fun onAppStopped() {
        backgroundedAt = SystemClock.elapsedRealtime()
    }

    /** Uygulama öne geldiğinde otomatik kilit süresini değerlendirir. */
    fun onAppStarted() {
        if (!_unlocked.value) return
        val autoLock = settings.value.autoLock
        val elapsedMin = (SystemClock.elapsedRealtime() - backgroundedAt) / 60_000
        if (autoLock == AutoLock.IMMEDIATELY || elapsedMin >= autoLock.minutes) {
            lock()
        }
    }

    /* --------------------------- Onboarding ----------------------------- */
    fun completeOnboarding(name: String, birthDate: LocalDate?, gender: Gender?) {
        viewModelScope.launch {
            settingsStore.update {
                it.copy(
                    onboarded = true,
                    profile = UserProfile(name = name.trim(), birthDate = birthDate, gender = gender),
                )
            }
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch { settingsStore.update { it.copy(onboarded = true) } }
    }

    /* ----------------------------- Ayarlar ------------------------------ */
    fun updateProfile(profile: UserProfile) =
        edit { it.copy(profile = profile) }

    fun setThemeMode(mode: ThemeMode) = edit { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = edit { it.copy(dynamicColor = enabled) }
    fun setLockEnabled(enabled: Boolean) = edit { it.copy(lockEnabled = enabled) }
    fun setLockLocation(location: LockLocation) = edit { it.copy(lockLocation = location) }
    fun setBiometricEnabled(enabled: Boolean) = edit { it.copy(biometricEnabled = enabled) }
    fun setAutoLock(autoLock: AutoLock) = edit { it.copy(autoLock = autoLock) }
    fun setPin(pin: String?) = edit { it.copy(pin = pin?.ifBlank { null }) }

    private fun edit(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsStore.update(transform) }
    }
}
