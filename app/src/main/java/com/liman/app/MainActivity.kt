package com.liman.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liman.app.data.model.ThemeMode
import com.liman.app.ui.LimanApp
import com.liman.app.ui.LimanViewModel
import com.liman.app.ui.copy.LocalCopy
import com.liman.app.ui.copy.copyFor
import com.liman.app.ui.theme.LimanTheme

class MainActivity : FragmentActivity() {

    private val viewModel: LimanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ayarlar yüklenene kadar açılış ekranını tut.
        splash.setKeepOnScreenCondition { !viewModel.ready.value }

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val copy = remember(settings.profile.gender) { copyFor(settings.profile.gender) }
            LimanTheme(
                darkTheme = dark,
                palette = settings.themePalette,
                dynamicColor = settings.dynamicColor,
            ) {
                CompositionLocalProvider(LocalCopy provides copy) {
                    LimanApp(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppStarted()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppStopped()
    }
}
