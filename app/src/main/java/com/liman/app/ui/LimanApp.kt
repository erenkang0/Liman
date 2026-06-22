package com.liman.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liman.app.data.model.LockLocation
import com.liman.app.ui.bonds.AddContactScreen
import com.liman.app.ui.bonds.ContactProfileScreen
import com.liman.app.ui.calm.CalmScreen
import com.liman.app.ui.lock.LockReason
import com.liman.app.ui.lock.LockScreen
import com.liman.app.ui.bonds.MemoryDetailScreen
import com.liman.app.ui.me.JournalEditorScreen
import com.liman.app.ui.me.JournalViewerScreen
import com.liman.app.ui.mood.MoodEntryScreen
import com.liman.app.ui.navigation.Routes
import com.liman.app.ui.onboarding.OnboardingScreen
import com.liman.app.ui.settings.RemindersScreen
import com.liman.app.ui.settings.SettingsScreen
import com.liman.app.ui.tools.BreathingScreen
import com.liman.app.ui.tools.GratitudeScreen
import com.liman.app.ui.tools.MoodCalendarScreen
import com.liman.app.ui.tools.ThoughtRecordScreen
import com.liman.app.ui.tools.TimeCapsuleScreen

@Composable
fun LimanApp(viewModel: LimanViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val unlocked by viewModel.unlocked.collectAsStateWithLifecycle()

    // Uygulama açılışı kilidi (kullanıcı seçerse).
    val appOpenLock = settings.onboarded &&
        settings.lockEnabled &&
        settings.lockLocation == LockLocation.APP_OPEN &&
        !unlocked

    // Kök Surface, doğru içerik rengini (onBackground) sağlar; böylece Scaffold
    // dışındaki ekranlarda (onboarding / açılış kilidi) yazılar koyu temada da okunur.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        when {
            !settings.onboarded -> OnboardingScreen(
                onComplete = viewModel::completeOnboarding,
                onSkip = viewModel::skipOnboarding,
            )

            appOpenLock -> LockScreen(
                settings = settings,
                reason = LockReason.APP_OPEN,
                onUnlock = viewModel::unlock,
            )

            else -> LimanNavHost(viewModel)
        }
    }
}

@Composable
private fun LimanNavHost(viewModel: LimanViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        enterTransition = {
            slideInVertically(tween(280)) { it / 8 } + fadeIn(tween(280))
        },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = {
            slideOutVertically(tween(240)) { it / 8 } + fadeOut(tween(220))
        },
    ) {
        composable(Routes.MAIN) {
            MainScaffold(rootNavController = navController, viewModel = viewModel)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenReminders = { navController.navigate(Routes.REMINDERS) },
            )
        }

        composable(Routes.REMINDERS) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CALM) {
            CalmScreen(
                onBack = { navController.popBackStack() },
                onOpenBreathing = { navController.navigate(Routes.TOOL_BREATHING) },
                onOpenBonds = { navController.popBackStack() },
            )
        }

        composable(Routes.MOOD_ENTRY) {
            MoodEntryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.JOURNAL_EDITOR) {
            JournalEditorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(Routes.ADD_CONTACT) {
            AddContactScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.EDIT_CONTACT_ROUTE,
            arguments = listOf(navArgument(Routes.CONTACT_ARG) { type = NavType.StringType }),
        ) { entry ->
            AddContactScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                editContactId = entry.arguments?.getString(Routes.CONTACT_ARG),
            )
        }

        composable(
            route = Routes.CONTACT_ROUTE,
            arguments = listOf(navArgument(Routes.CONTACT_ARG) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Routes.CONTACT_ARG).orEmpty()
            ContactProfileScreen(
                viewModel = viewModel,
                contactId = id,
                onBack = { navController.popBackStack() },
                onOpenMemory = { memoryId -> navController.navigate(Routes.memory(id, memoryId)) },
            )
        }

        composable(
            route = Routes.JOURNAL_VIEWER_ROUTE,
            arguments = listOf(navArgument(Routes.JOURNAL_ARG) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Routes.JOURNAL_ARG).orEmpty()
            JournalViewerScreen(
                viewModel = viewModel,
                journalId = id,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.MEMORY_ROUTE,
            arguments = listOf(
                navArgument(Routes.MEMORY_CONTACT_ARG) { type = NavType.StringType },
                navArgument(Routes.MEMORY_ARG) { type = NavType.StringType },
            ),
        ) { entry ->
            MemoryDetailScreen(
                viewModel = viewModel,
                contactId = entry.arguments?.getString(Routes.MEMORY_CONTACT_ARG).orEmpty(),
                memoryId = entry.arguments?.getString(Routes.MEMORY_ARG).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.TOOL_BREATHING) {
            BreathingScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.TOOL_THOUGHT) {
            ThoughtRecordScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.TOOL_GRATITUDE) {
            GratitudeScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.TOOL_CAPSULE) {
            TimeCapsuleScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.TOOL_CALENDAR) {
            MoodCalendarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
