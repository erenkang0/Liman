package com.liman.app

import android.app.Application
import android.content.Context
import com.liman.app.data.crypto.CryptoManager
import com.liman.app.data.local.SettingsStore
import com.liman.app.data.repository.LimanRepository

/**
 * Basit, elle bağımlılık konteyneri. (Bir DI çerçevesi yerine; uygulama küçük
 * ve bağımlılıklar tekil.)
 */
class AppContainer(context: Context) {
    val crypto: CryptoManager by lazy { CryptoManager() }
    val repository: LimanRepository by lazy { LimanRepository(crypto) }
    val settingsStore: SettingsStore by lazy { SettingsStore(context.applicationContext) }
}

class LimanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
