package com.liman.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.liman.app.data.model.AppSettings
import com.liman.app.data.model.LockLocation
import com.liman.app.data.notifications.LimanNotifications
import com.liman.app.ui.bonds.BondsScreen
import com.liman.app.ui.bonds.DefterSummaryScreen
import com.liman.app.ui.components.LockBadge
import com.liman.app.ui.components.TimeOfDay
import com.liman.app.ui.components.WorldBackground
import com.liman.app.ui.components.bounceClick
import com.liman.app.ui.components.currentTimeOfDay
import com.liman.app.ui.insight.InsightScreen
import com.liman.app.ui.insight.buildInsightShareText
import com.liman.app.ui.lock.LockReason
import com.liman.app.ui.lock.LockScreen
import com.liman.app.ui.me.MeScreen
import com.liman.app.ui.navigation.Routes
import com.liman.app.ui.navigation.Tab
import com.liman.app.ui.navigation.World
import com.liman.app.ui.theme.DefterTheme
import com.liman.app.ui.theme.LimanTheme
import com.liman.app.ui.theme.LocalLimanColors
import com.liman.app.ui.today.TodayScreen
import kotlinx.coroutines.launch

private data class FabSpec(
    val icon: ImageVector,
    val description: String,
    val container: Color,
    val onContainer: Color,
    val cornerRadius: Int,
    val onClick: () -> Unit,
)

@Composable
fun MainScaffold(
    rootNavController: NavController,
    viewModel: LimanViewModel,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val timeOfDay = remember { currentTimeOfDay() }

    var currentWorld by rememberSaveable { mutableStateOf(World.DEFTER) }
    var defterTab by rememberSaveable { mutableStateOf(Tab.PEOPLE) }
    var limanTab by rememberSaveable { mutableStateOf(Tab.TODAY) }

    // Başlangıçta bildirim izni iste (Android 13+).
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* sonucu sessizce karşıla */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !LimanNotifications.hasPermission(context)
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Crossfade(
        targetState = currentWorld,
        animationSpec = tween(durationMillis = 420),
        label = "world",
    ) { world ->
        val selectedTab = if (world == World.DEFTER) defterTab else limanTab
        val content: @Composable () -> Unit = {
            WorldScaffold(
                world = world,
                selectedTab = selectedTab,
                onSelectTab = { if (world == World.DEFTER) defterTab = it else limanTab = it },
                onSwitchWorld = { currentWorld = it },
                rootNavController = rootNavController,
                viewModel = viewModel,
                settings = settings,
                timeOfDay = timeOfDay,
            )
        }
        if (world == World.DEFTER) {
            DefterTheme { content() }
        } else {
            val dark = when (settings.themeMode) {
                com.liman.app.data.model.ThemeMode.LIGHT -> false
                com.liman.app.data.model.ThemeMode.DARK -> true
                com.liman.app.data.model.ThemeMode.SYSTEM ->
                    androidx.compose.foundation.isSystemInDarkTheme()
            }
            LimanTheme(
                darkTheme = dark,
                palette = settings.themePalette,
                dynamicColor = settings.dynamicColor,
            ) { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorldScaffold(
    world: World,
    selectedTab: Tab,
    onSelectTab: (Tab) -> Unit,
    onSwitchWorld: (World) -> Unit,
    rootNavController: NavController,
    viewModel: LimanViewModel,
    settings: AppSettings,
    timeOfDay: TimeOfDay,
) {
    val unlocked by viewModel.unlocked.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val limanColors = LocalLimanColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showQuickAdd by remember { mutableStateOf(false) }

    val defterLocked = world == World.DEFTER && settings.lockEnabled && !unlocked
    val innerWorldLocked = settings.lockEnabled &&
        settings.lockLocation == LockLocation.INNER_WORLD_ONLY && !unlocked

    fun openJournalOrUnlock() {
        if (unlocked || !settings.lockEnabled) rootNavController.navigate(Routes.JOURNAL_EDITOR)
        else onSelectTab(Tab.ME)
    }

    fun shareInsight() {
        val text = buildInsightShareText(
            moods = viewModel.repository.moods.value,
            name = settings.profile.name,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "İçgörünü paylaş"))
    }

    val fab: FabSpec? = if (defterLocked) null else when (selectedTab) {
        Tab.PEOPLE -> FabSpec(
            icon = Icons.Rounded.PersonAdd,
            description = "Kişi ekle",
            container = scheme.primary,
            onContainer = scheme.onPrimary,
            cornerRadius = 40,
            onClick = { rootNavController.navigate(Routes.ADD_CONTACT) },
        )
        Tab.SUMMARY -> null
        Tab.TODAY -> FabSpec(
            icon = Icons.Rounded.Add,
            description = "Hızlı ekle",
            container = scheme.primary,
            onContainer = scheme.onPrimary,
            cornerRadius = 20,
            onClick = { showQuickAdd = true },
        )
        Tab.ME -> if (innerWorldLocked) null else FabSpec(
            icon = Icons.Rounded.EditNote,
            description = "Bugüne yaz",
            container = scheme.primary,
            onContainer = scheme.onPrimary,
            cornerRadius = 28,
            onClick = { rootNavController.navigate(Routes.JOURNAL_EDITOR) },
        )
        Tab.INSIGHT -> FabSpec(
            icon = Icons.Rounded.Share,
            description = "Paylaş",
            container = scheme.tertiaryContainer,
            onContainer = scheme.onTertiaryContainer,
            cornerRadius = 16,
            onClick = { shareInsight() },
        )
    }

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { WorldSwitcher(world = world, onSwitch = onSwitchWorld) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        bottomBar = {
            if (!defterLocked) {
                NavigationBar(containerColor = scheme.surfaceContainer) {
                    Tab.of(world).forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onSelectTab(tab) },
                            icon = {
                                Box {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.icon,
                                        contentDescription = tab.label,
                                    )
                                    if (tab.showLockBadge && settings.lockEnabled) {
                                        LockBadge(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 7.dp, y = (-5).dp),
                                            size = 13,
                                        )
                                    }
                                }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = scheme.onSecondaryContainer,
                                indicatorColor = scheme.secondaryContainer,
                                selectedTextColor = scheme.onSurface,
                                unselectedIconColor = scheme.onSurfaceVariant,
                                unselectedTextColor = scheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = { if (fab != null) AdaptiveFab(fab) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            WorldBackground(world = world, timeOfDay = timeOfDay, modifier = Modifier.fillMaxSize())

            if (defterLocked) {
                LockScreen(
                    settings = settings,
                    reason = LockReason.DEFTER,
                    onUnlock = viewModel::unlock,
                )
            } else {
                Crossfade(
                    targetState = selectedTab,
                    modifier = Modifier.fillMaxSize(),
                    animationSpec = tween(durationMillis = 280),
                    label = "tabContent",
                ) { tab ->
                    when (tab) {
                        Tab.PEOPLE -> BondsScreen(
                            viewModel = viewModel,
                            onAddContact = { rootNavController.navigate(Routes.ADD_CONTACT) },
                            onOpenContact = { rootNavController.navigate(Routes.contact(it)) },
                        )

                        Tab.SUMMARY -> DefterSummaryScreen(
                            viewModel = viewModel,
                            onOpenContact = { rootNavController.navigate(Routes.contact(it)) },
                        )

                        Tab.TODAY -> TodayScreen(
                            viewModel = viewModel,
                            onOpenSettings = { rootNavController.navigate(Routes.SETTINGS) },
                            onQuickMood = { rootNavController.navigate(Routes.MOOD_ENTRY) },
                            onOpenInnerWorld = { onSelectTab(Tab.ME) },
                            onOpenBonds = { onSwitchWorld(World.DEFTER) },
                            onOpenTool = { rootNavController.navigate(it) },
                            onOpenContact = { rootNavController.navigate(Routes.contact(it)) },
                            onOpenCalm = { rootNavController.navigate(Routes.CALM) },
                            onOpenSearch = { rootNavController.navigate(Routes.SEARCH) },
                        )

                        Tab.ME -> if (innerWorldLocked) {
                            LockScreen(
                                settings = settings,
                                reason = LockReason.INNER_WORLD,
                                onUnlock = viewModel::unlock,
                            )
                        } else {
                            MeScreen(
                                viewModel = viewModel,
                                onNewJournal = { rootNavController.navigate(Routes.JOURNAL_EDITOR) },
                                onOpenTool = { rootNavController.navigate(it) },
                                onOpenJournal = { rootNavController.navigate(Routes.journal(it)) },
                            )
                        }

                        Tab.INSIGHT -> InsightScreen(
                            viewModel = viewModel,
                            onShare = { shareInsight() },
                            onOpenReport = { rootNavController.navigate(Routes.WEEKLY_REPORT) },
                        )
                    }
                }
            }
        }
    }

    if (showQuickAdd) {
        val sheetState = rememberModalBottomSheetState()
        fun dismiss() {
            scope.launch { sheetState.hide() }.invokeOnCompletion { showQuickAdd = false }
        }
        ModalBottomSheet(
            onDismissRequest = { showQuickAdd = false },
            sheetState = sheetState,
            containerColor = scheme.surfaceContainerLow,
        ) {
            Column(Modifier.padding(bottom = 24.dp)) {
                Text(
                    "Hızlı ekle",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 4.dp),
                )
                QuickAddRow(Icons.Rounded.Mood, "Ruh hali kaydet", "Bugün nasılsın?") {
                    dismiss(); rootNavController.navigate(Routes.MOOD_ENTRY)
                }
                QuickAddRow(Icons.Rounded.EditNote, "Günlüğe yaz", "İç dünyana bir not") {
                    dismiss(); openJournalOrUnlock()
                }
                QuickAddRow(Icons.Rounded.PersonAdd, "Kişi ekle", "Defterine bir kişi") {
                    dismiss(); rootNavController.navigate(Routes.ADD_CONTACT)
                }
                QuickAddRow(Icons.Rounded.SelfImprovement, "Nefes egzersizi", "Bir an dur, nefes al") {
                    dismiss(); rootNavController.navigate(Routes.TOOL_BREATHING)
                }
                QuickAddRow(Icons.Rounded.Spa, "Sakinleş", "Bunaldıysan hızlı destek") {
                    dismiss(); rootNavController.navigate(Routes.CALM)
                }
            }
        }
    }
}

/** Üstteki iki dünya arası geçiş "pill"i: [ Defter | Liman ]. */
@Composable
private fun WorldSwitcher(world: World, onSwitch: (World) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
    ) {
        Row(Modifier.padding(4.dp)) {
            World.entries.forEach { w ->
                val selected = w == world
                val bg by animateColorAsState(
                    if (selected) scheme.primary else Color.Transparent,
                    label = "segBg",
                )
                val fg by animateColorAsState(
                    if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                    label = "segFg",
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bg,
                    modifier = Modifier.bounceClick { if (!selected) onSwitch(w) },
                ) {
                    Text(
                        w.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = fg,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdaptiveFab(spec: FabSpec) {
    val container by animateColorAsState(spec.container, label = "fabColor")
    val corner by animateDpAsState(spec.cornerRadius.dp, label = "fabCorner")
    FloatingActionButton(
        onClick = spec.onClick,
        containerColor = container,
        contentColor = spec.onContainer,
        shape = RoundedCornerShape(corner),
    ) {
        AnimatedContent(
            targetState = spec.icon,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "fabIcon",
        ) { icon ->
            Icon(icon, contentDescription = spec.description)
        }
    }
}

@Composable
private fun QuickAddRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    )
}
