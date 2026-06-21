package com.liman.app.data.model

import java.time.LocalDate

/** Kapsayıcı cinsiyet seçenekleri. */
enum class Gender(val label: String) {
    FEMALE("Kadın"),
    MALE("Erkek"),
    OTHER("Diğer"),
    UNSPECIFIED("Belirtmek istemiyorum"),
}

/** Tüm bilgiler yalnızca cihazda saklanır. */
data class UserProfile(
    val name: String = "",
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
)

enum class ThemeMode(val label: String) {
    SYSTEM("Sistem"),
    LIGHT("Açık"),
    DARK("Koyu"),
}

/** Kilit nerede devreye girsin? */
enum class LockLocation(val label: String, val description: String) {
    INNER_WORLD_ONLY("Yalnızca iç dünya", "Bağlar kilitsiz kalır; kilit yalnızca Ben'e girerken"),
    APP_OPEN("Uygulama açılışında", "Liman'ı her açtığında kilit ekranı gelir"),
}

/** Otomatik kilit süresi. */
enum class AutoLock(val label: String, val minutes: Int) {
    IMMEDIATELY("Hemen", 0),
    ONE_MIN("1 dakika", 1),
    FIVE_MIN("5 dakika", 5),
    FIFTEEN_MIN("15 dakika", 15),
}

/** Her bildirim türü kendi açık/kapalı + zaman ayarına sahiptir. */
data class NotificationSetting(
    val enabled: Boolean,
    val time: String,      // "HH:mm" — gösterim amaçlı
)

data class NotificationPrefs(
    val gentleReminder: NotificationSetting = NotificationSetting(true, "21:00"),
    val moodCheckIn: NotificationSetting = NotificationSetting(true, "13:00"),
    val birthdays: NotificationSetting = NotificationSetting(true, "09:00"),
    val bondReminder: NotificationSetting = NotificationSetting(false, "18:00"),
    val quietHoursEnabled: Boolean = true,
    val quietFrom: String = "22:30",
    val quietTo: String = "08:00",
)

data class AppSettings(
    val profile: UserProfile = UserProfile(),
    val onboarded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val lockEnabled: Boolean = true,
    val lockLocation: LockLocation = LockLocation.INNER_WORLD_ONLY,
    val biometricEnabled: Boolean = true,
    val pin: String? = null,                 // demo amaçlı; üretimde Keystore ile saklanmalı
    val autoLock: AutoLock = AutoLock.FIVE_MIN,
    val notifications: NotificationPrefs = NotificationPrefs(),
) {
    val pinIsSet: Boolean get() = !pin.isNullOrBlank()
}
