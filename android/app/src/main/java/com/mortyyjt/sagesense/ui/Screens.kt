package com.mortyyjt.sagesense.ui

import android.Manifest
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mortyyjt.sagesense.R
import com.mortyyjt.sagesense.data.LearnCard
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.data.WatchlistEntity
import com.mortyyjt.sagesense.risk.RiskLevel
import com.mortyyjt.sagesense.service.AlertNotifier
import com.mortyyjt.sagesense.service.SageNotificationListenerService
import com.mortyyjt.sagesense.ui.theme.ThemeMode
import com.mortyyjt.sagesense.ui.theme.sageStatusColors
import java.text.DateFormat
import java.util.Date

private data class PermissionState(
    val notificationAccess: Boolean,
    val notificationPosting: Boolean,
    val callScreening: Boolean,
    val callScreeningAvailable: Boolean,
)

private data class BottomDestination(val route: String, val icon: @Composable () -> Unit)

private fun l(locale: String, en: String, zh: String): String = if (locale == "zh-CN") zh else en

@Composable
private fun sageCardElevation() = CardDefaults.cardElevation(defaultElevation = 2.dp)

@Composable
fun SageSenseApp(
    viewModel: SageSenseViewModel,
    initialEventId: String?,
    onDeepLinkConsumed: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val agentState by viewModel.agentState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permissionState by remember { mutableStateOf(context.readPermissionState()) }
    var showPermissionSetup by rememberSaveable { mutableStateOf(false) }
    var callRoleNeedsSettings by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionState = context.readPermissionState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val postNotificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionState = context.readPermissionState()
    }
    val openDefaultAppsSettings = {
        try {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val updatedPermissions = context.readPermissionState()
        permissionState = updatedPermissions
        if (updatedPermissions.callScreening) {
            callRoleNeedsSettings = false
        } else {
            callRoleNeedsSettings = true
            openDefaultAppsSettings()
        }
    }
    val openNotificationAccess = {
        val serviceComponent = ComponentName(context, SageNotificationListenerService::class.java)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, serviceComponent.flattenToString())
            }
        } else {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
    val requestNotifications = {
        if (Build.VERSION.SDK_INT >= 33) postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    val openNotificationSettings = {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}"),
            )
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    val requestCallRole = {
        if (Build.VERSION.SDK_INT >= 29) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (callRoleNeedsSettings) {
                    openDefaultAppsSettings()
                } else {
                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                }
            }
        }
    }
    val reviewNotificationPermission = {
        if (permissionState.notificationPosting) openNotificationSettings() else requestNotifications()
    }
    val reviewCallRole = {
        if (permissionState.callScreening || callRoleNeedsSettings) openDefaultAppsSettings() else requestCallRole()
    }

    if (!state.ready) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (!state.onboardingComplete) {
        Box(Modifier.fillMaxSize()) {
            OnboardingScreen(
                locale = state.locale,
                permissions = permissionState,
                onLanguage = viewModel::setLanguage,
                onNotificationAccess = { showPermissionSetup = true },
                onNotificationPermission = { showPermissionSetup = true },
                onCallRole = { showPermissionSetup = true },
                onContinue = viewModel::completeOnboarding,
            )
            if (showPermissionSetup) {
                PermissionSetupDialog(
                    locale = state.locale,
                    permissions = permissionState,
                    callRoleNeedsSettings = callRoleNeedsSettings,
                    onNotificationAccess = openNotificationAccess,
                    onNotificationPermission = reviewNotificationPermission,
                    onCallRole = reviewCallRole,
                    onDismiss = { showPermissionSetup = false },
                )
            }
        }
        return
    }

    var knownEventIds by remember { mutableStateOf<Set<String>?>(null) }
    var cognitivePauseEventId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.events) {
        val currentIds = state.events.mapTo(mutableSetOf()) { it.id }
        val previousIds = knownEventIds
        if (previousIds == null) {
            knownEventIds = currentIds
        } else {
            val newestRisk = state.events.firstOrNull {
                it.id !in previousIds && it.riskLevel != RiskLevel.LOW
            }
            knownEventIds = currentIds
            if (cognitivePauseEventId == null) cognitivePauseEventId = newestRisk?.id
        }
    }
    val cognitivePauseEvent = state.events.firstOrNull { it.id == cognitivePauseEventId }

    val navController = rememberNavController()
    LaunchedEffect(initialEventId, state.events) {
        val eventId = initialEventId ?: return@LaunchedEffect
        if (state.events.any { it.id == eventId }) {
            navController.navigate("detail/$eventId")
            onDeepLinkConsumed()
        }
    }

    MainScaffold(
        navController = navController,
        state = state,
        agentState = agentState,
        permissions = permissionState,
        cognitivePauseEvent = cognitivePauseEvent,
        onNotificationAccess = { showPermissionSetup = true },
        onNotificationPermission = { showPermissionSetup = true },
        onCallRole = { showPermissionSetup = true },
        onLanguage = viewModel::setLanguage,
        onThemeMode = viewModel::setThemeMode,
        onClearHistory = viewModel::clearHistory,
        onAskAgent = viewModel::askAgent,
        onResetAgent = viewModel::resetAgent,
        onDismissCognitivePause = { cognitivePauseEventId = null },
    )
    if (showPermissionSetup) {
        PermissionSetupDialog(
            locale = state.locale,
            permissions = permissionState,
            callRoleNeedsSettings = callRoleNeedsSettings,
            onNotificationAccess = openNotificationAccess,
            onNotificationPermission = reviewNotificationPermission,
            onCallRole = reviewCallRole,
            onDismiss = { showPermissionSetup = false },
        )
    }
}

private fun Context.readPermissionState(): PermissionState {
    val listenerComponent = ComponentName(this, SageNotificationListenerService::class.java)
    val notificationAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        getSystemService(NotificationManager::class.java)
            .isNotificationListenerAccessGranted(listenerComponent)
    } else {
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }
    val notificationPosting = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val roleManager = if (Build.VERSION.SDK_INT >= 29) getSystemService(RoleManager::class.java) else null
    val available = Build.VERSION.SDK_INT >= 29 && roleManager?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true
    val held = available && roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    return PermissionState(notificationAccess, notificationPosting, held, available)
}

@Composable
private fun PermissionSetupDialog(
    locale: String,
    permissions: PermissionState,
    callRoleNeedsSettings: Boolean,
    onNotificationAccess: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCallRole: () -> Unit,
    onDismiss: () -> Unit,
) {
    val total = if (permissions.callScreeningAvailable) 3 else 2
    val enabled = listOf(
        permissions.notificationAccess,
        permissions.notificationPosting,
        permissions.callScreeningAvailable && permissions.callScreening,
    ).take(total).count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp)) },
        title = { Text(l(locale, "Set up protection", "设置防诈保护")) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    l(
                        locale,
                        "$enabled of $total protections are enabled. You can review or turn off each permission in Android Settings.",
                        "已开启 $enabled/$total 项保护。你可以随时前往 Android 系统设置查看或关闭每项权限。",
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
                PermissionDialogRow(
                    title = l(locale, "Read message notifications", "读取消息通知"),
                    description = l(locale, "Checks supported notifications locally for scam signals.", "在手机本地检查受支持的通知是否包含诈骗信号。"),
                    granted = permissions.notificationAccess,
                    locale = locale,
                    actionLabel = if (permissions.notificationAccess) {
                        l(locale, "Review or turn off", "查看或关闭")
                    } else {
                        l(locale, "Open SageSense switch", "打开 SageSense 开关")
                    },
                    onClick = onNotificationAccess,
                )
                PermissionDialogRow(
                    title = l(locale, "Show risk warnings", "显示风险警告"),
                    description = l(locale, "Lets SageSense display a warning when it detects risk.", "发现风险时，允许 SageSense 显示警告通知。"),
                    granted = permissions.notificationPosting,
                    locale = locale,
                    actionLabel = if (permissions.notificationPosting) {
                        l(locale, "Review or turn off", "查看或关闭")
                    } else {
                        l(locale, "Allow", "去授权")
                    },
                    onClick = onNotificationPermission,
                )
                if (permissions.callScreeningAvailable) {
                    PermissionDialogRow(
                        title = l(locale, "Warn about incoming calls", "来电风险警告"),
                        description = if (callRoleNeedsSettings) {
                            l(
                                locale,
                                "Android blocked repeated requests. Open Default apps, choose Caller ID & spam app, then select SageSense.",
                                "Android 已阻止重复申请。请打开默认应用，选择“来电显示和骚扰电话应用”，然后选择 SageSense。",
                            )
                        } else {
                            l(locale, "Calls keep ringing; SageSense only checks the number and warns you.", "电话仍会继续响铃；SageSense 只检查号码并发出警告。")
                        },
                        granted = permissions.callScreening,
                        locale = locale,
                        actionLabel = if (permissions.callScreening) {
                            l(locale, "Review or turn off", "查看或关闭")
                        } else if (callRoleNeedsSettings) {
                            l(locale, "Open Default apps", "打开默认应用设置")
                        } else {
                            l(locale, "Allow", "去授权")
                        },
                        onClick = onCallRole,
                    )
                }
                Text(
                    l(
                        locale,
                        "Permissions are optional. Features that need a disabled permission will show OFF; History and Learn remain available.",
                        "所有权限均为可选。依赖已关闭权限的功能会显示“未开启”；记录和学习功能仍可使用。",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(l(locale, "Done", "完成"))
            }
        },
    )
}

@Composable
private fun PermissionDialogRow(
    title: String,
    description: String,
    granted: Boolean,
    locale: String,
    actionLabel: String = l(locale, "Allow", "去授权"),
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (granted) MaterialTheme.sageStatusColors.successContainer else MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = sageCardElevation(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    null,
                    tint = if (granted) MaterialTheme.sageStatusColors.success else MaterialTheme.sageStatusColors.warning,
                    modifier = Modifier.size(26.dp),
                )
                Text(title, modifier = Modifier.weight(1f).padding(start = 9.dp), fontWeight = FontWeight.Bold)
                Text(
                    if (granted) l(locale, "ON", "已开启") else l(locale, "OFF", "未开启"),
                    color = if (granted) MaterialTheme.sageStatusColors.success else MaterialTheme.sageStatusColors.warning,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(description, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    state: SageSenseUiState,
    agentState: AgentUiState,
    permissions: PermissionState,
    cognitivePauseEvent: RiskEventEntity?,
    onNotificationAccess: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCallRole: () -> Unit,
    onLanguage: (String) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onClearHistory: () -> Unit,
    onAskAgent: (String, String?) -> Unit,
    onResetAgent: () -> Unit,
    onDismissCognitivePause: () -> Unit,
) {
    val destinations = listOf(
        BottomDestination("home") { Icon(Icons.Default.Home, null) },
        BottomDestination("history") { Icon(Icons.Default.History, null) },
        BottomDestination("learn") { Icon(Icons.Default.School, null) },
        BottomDestination("settings") { Icon(Icons.Default.Settings, null) },
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in destinations.map { it.route }
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        destinations.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                alwaysShowLabel = true,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = destination.icon,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.surface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                label = {
                                    Text(
                                        when (destination.route) {
                                            "home" -> l(state.locale, "Home", "首页")
                                            "history" -> l(state.locale, "History", "记录")
                                            "learn" -> l(state.locale, "Learn", "学习")
                                            else -> l(state.locale, "Settings", "设置")
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding),
            ) {
            composable("home") {
                HomeScreen(
                    state = state,
                    permissions = permissions,
                    onNotificationAccess = onNotificationAccess,
                    onNotificationPermission = onNotificationPermission,
                    onCallRole = onCallRole,
                    onOpenEvent = { navController.navigate("detail/$it") },
                    onAgent = { navController.navigate("agent") },
                )
            }
            composable("history") {
                HistoryScreen(state, onOpenEvent = { navController.navigate("detail/$it") })
            }
            composable("learn") { LearnScreen(state.locale) }
            composable("settings") {
                SettingsScreen(
                    locale = state.locale,
                    eventsCount = state.events.size,
                    permissions = permissions,
                    themeMode = state.themeMode,
                    onLanguage = onLanguage,
                    onThemeMode = onThemeMode,
                    onNotificationAccess = onNotificationAccess,
                    onNotificationPermission = onNotificationPermission,
                    onCallRole = onCallRole,
                    onClearHistory = onClearHistory,
                    onFaq = { navController.navigate("faq-safety") },
                )
            }
            composable("faq-safety") {
                FaqSafetyScreen(
                    locale = state.locale,
                    onLanguage = onLanguage,
                    onBack = navController::popBackStack,
                )
            }
            composable(
                route = "detail/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { entry ->
                val eventId = entry.arguments?.getString("eventId")
                EventDetailScreen(
                    locale = state.locale,
                    event = state.events.firstOrNull { it.id == eventId },
                    related = state.events.filter { candidate ->
                        candidate.id != eventId && candidate.relatedCampaignId != null &&
                            candidate.relatedCampaignId == state.events.firstOrNull { it.id == eventId }?.relatedCampaignId
                    },
                    onBack = navController::popBackStack,
                    onAsk = { navController.navigate("agent?eventId=$eventId") },
                )
            }
            composable(
                route = "agent?eventId={eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType; nullable = true }),
            ) { entry ->
                AgentScreen(
                    locale = state.locale,
                    event = state.events.firstOrNull { it.id == entry.arguments?.getString("eventId") },
                    agentState = agentState,
                    onBack = navController::popBackStack,
                    onAsk = { question -> onAskAgent(question, entry.arguments?.getString("eventId")) },
                    onReset = onResetAgent,
                )
            }
            }
        }
        CognitivePauseExperience(
            event = cognitivePauseEvent,
            locale = state.locale,
            onLanguage = onLanguage,
            onSeeWhy = { eventId ->
                onDismissCognitivePause()
                navController.navigate("detail/$eventId")
            },
            onDismiss = onDismissCognitivePause,
        )
    }
}

@Composable
private fun OnboardingScreen(
    locale: String,
    permissions: PermissionState,
    onLanguage: (String) -> Unit,
    onNotificationAccess: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCallRole: () -> Unit,
    onContinue: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                EyeShieldMascot(80.dp)
                Column {
                    Text("SageSense", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text(l(locale, "Your safety companion", "你的防诈伙伴"), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Text(
                l(locale, "Protection that explains itself", "会解释原因的自动防护"),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                l(
                    locale,
                    "Risk checks happen on this phone. Only a redacted event is sent when you choose to ask the advisor.",
                    "风险判断在这台手机上完成。只有当你主动询问 Agent 时，才会发送脱敏后的事件摘要。",
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        item {
            PermissionCard(
                title = l(locale, "Message notifications", "消息通知"),
                description = l(locale, "Allow SageSense to check supported message notifications.", "允许 SageSense 检查受支持的消息通知。"),
                granted = permissions.notificationAccess,
                onClick = onNotificationAccess,
                locale = locale,
            )
        }
        if (!permissions.notificationPosting) {
            item {
                PermissionCard(
                    title = l(locale, "Warning notifications", "风险警告通知"),
                    description = l(locale, "Allow visible warnings when risk is found.", "发现风险时显示醒目的警告。"),
                    granted = false,
                    onClick = onNotificationPermission,
                    locale = locale,
                )
            }
        }
        if (permissions.callScreeningAvailable) {
            item {
                PermissionCard(
                    title = l(locale, "Incoming call warnings", "来电风险警告"),
                    description = l(locale, "Calls keep ringing. SageSense only shows a warning.", "电话会继续响铃，SageSense 只显示警告。"),
                    granted = permissions.callScreening,
                    onClick = onCallRole,
                    locale = locale,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = locale == "en-AU", onClick = { onLanguage("en-AU") }, label = { Text("English") })
                FilterChip(selected = locale == "zh-CN", onClick = { onLanguage("zh-CN") }, label = { Text("中文") })
            }
        }
        item {
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
                Text(l(locale, "Continue to SageSense", "进入 SageSense"))
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit,
    locale: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (granted) MaterialTheme.sageStatusColors.successContainer else MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = sageCardElevation(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    if (granted) Icons.Default.CheckCircle else Icons.Default.Shield,
                    null,
                    tint = if (granted) MaterialTheme.sageStatusColors.success else MaterialTheme.colorScheme.primary,
                )
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Text(description, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onClick, modifier = Modifier.heightIn(min = 56.dp)) {
                Text(if (granted) l(locale, "Review access", "查看授权") else l(locale, "Set up", "去设置"))
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: SageSenseUiState,
    permissions: PermissionState,
    onNotificationAccess: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCallRole: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onAgent: () -> Unit,
) {
    val locale = state.locale
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("SageSense", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(l(locale, "Protection you can understand", "看得懂的防诈保护"), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = sageCardElevation(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    EyeShieldMascot(112.dp)
                    Text(
                        l(locale, "Your safety companion", "你的防诈伙伴"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onAgent, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)) {
                        Icon(Icons.Default.Chat, null)
                        Spacer(Modifier.width(8.dp))
                        Text(l(locale, "Ask SageSense", "询问 SageSense"))
                    }
                }
            }
        }
        item {
            Text(l(locale, "Automatic protection", "自动防护"), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            StatusRow(Icons.Default.Notifications, l(locale, "Message notifications", "消息通知"), permissions.notificationAccess) {
                onNotificationAccess()
            }
            StatusRow(Icons.Default.Call, l(locale, "Incoming call warnings", "来电风险警告"), permissions.callScreening) {
                if (permissions.callScreeningAvailable) onCallRole()
            }
            if (!permissions.notificationPosting) {
                StatusRow(Icons.Default.Warning, l(locale, "Visible warning permission", "警告显示权限"), false) {
                    onNotificationPermission()
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { AlertNotifier.postDemoScam(context) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text(l(locale, "Send seeded demo scam", "发送演示诈骗通知"))
            }
            Text(
                l(locale, "Clearly labelled test data — not a real intercepted message.", "这是明确标注的测试数据，不是真实拦截记录。"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(l(locale, "Recent checks", "最近检测"), style = MaterialTheme.typography.titleLarge)
                Text("${state.events.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        if (state.events.isEmpty()) {
            item { EmptyCard(l(locale, "No checks yet. Try the seeded demo above.", "还没有检测记录，可以先使用上面的演示通知。")) }
        } else {
            items(state.events.take(3), key = { it.id }) { event -> EventCard(event, locale) { onOpenEvent(event.id) } }
        }
    }
}

@Composable
private fun StatusRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = sageCardElevation(),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = if (enabled) MaterialTheme.sageStatusColors.success else MaterialTheme.sageStatusColors.warning,
                modifier = Modifier.size(30.dp),
            )
            Text(title, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.bodyLarge)
            Text(
                if (enabled) "ON" else "OFF",
                color = if (enabled) MaterialTheme.sageStatusColors.success else MaterialTheme.sageStatusColors.warning,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(state: SageSenseUiState, onOpenEvent: (String) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var level by remember { mutableStateOf<RiskLevel?>(null) }
    val locale = state.locale
    Column(Modifier.fillMaxSize()) {
        Text(l(locale, "Risk history", "风险记录"), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(20.dp))
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(l(locale, "Checks", "检测")) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(l(locale, "Watchlist", "风险观察名单")) })
        }
        if (tab == 0) {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        label = { Text(l(locale, "Search sender or message", "搜索发送方或内容")) },
                        singleLine = true,
                    )
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = level == null, onClick = { level = null }, label = { Text(l(locale, "All", "全部")) })
                        RiskLevel.entries.forEach { item ->
                            FilterChip(selected = level == item, onClick = { level = item }, label = { Text(riskLabel(item, locale)) })
                        }
                    }
                }
                val filtered = state.events.filter { event ->
                    (level == null || event.riskLevel == level) &&
                        (query.isBlank() || listOfNotNull(event.displaySender, event.redactedSnippet, *event.domains.toTypedArray())
                            .any { it.contains(query, ignoreCase = true) })
                }
                if (filtered.isEmpty()) item { EmptyCard(l(locale, "No matching risk events.", "没有符合条件的风险记录。")) }
                items(filtered, key = { it.id }) { event -> EventCard(event, locale) { onOpenEvent(event.id) } }
            }
        } else {
            WatchlistPanel(state.watchlist, locale)
        }
    }
}

@Composable
private fun WatchlistPanel(items: List<WatchlistEntity>, locale: String) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(
                l(locale, "Entries are local, source-labelled observations — not accusations about real people.", "所有条目都是带来源的本地观察记录，不代表对真实个人的指控。"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        items(items, key = { it.id }) { item ->
            Card(elevation = sageCardElevation()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.entityType == "phone") Icons.Default.Call else Icons.Default.Shield,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(item.value, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp))
                    }
                    Text(if (locale == "zh-CN") item.reasonZh else item.reasonEn, style = MaterialTheme.typography.bodyMedium)
                    Text(item.sourceTitle, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                    if (item.seededDemoData) DemoBadge(locale)
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: RiskEventEntity, locale: String, onClick: () -> Unit) {
    val colour = riskColour(event.riskLevel)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RiskBadge(event.riskLevel, locale)
                Spacer(Modifier.weight(1f))
                Text(formatTime(event.occurredAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
            Text(event.displaySender ?: l(locale, "Unknown sender", "未知发送方"), style = MaterialTheme.typography.titleMedium)
            Text(event.redactedSnippet, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
            Text("${event.riskScore}/100", color = colour, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (event.seededDemoData) DemoBadge(locale)
        }
    }
}

@Composable
private fun EventDetailScreen(
    locale: String,
    event: RiskEventEntity?,
    related: List<RiskEventEntity>,
    onBack: () -> Unit,
    onAsk: () -> Unit,
) {
    if (event == null) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            BackHeader(l(locale, "Risk detail", "风险详情"), onBack)
            EmptyCard(l(locale, "This event is no longer stored.", "这条记录已不存在。"))
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BackHeader(l(locale, "Risk detail", "风险详情"), onBack) }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = riskColour(event.riskLevel)),
                elevation = sageCardElevation(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, null, tint = riskOnColour(event.riskLevel), modifier = Modifier.size(46.dp))
                    Text(
                        l(locale, "${riskLabel(event.riskLevel, locale)} risk", "${riskLabel(event.riskLevel, locale)}风险"),
                        color = riskOnColour(event.riskLevel),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("${event.riskScore}/100", color = riskOnColour(event.riskLevel), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        item {
            Text(l(locale, "What SageSense saw", "SageSense 发现了什么"), style = MaterialTheme.typography.titleLarge)
            Card(elevation = sageCardElevation()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(event.displaySender ?: l(locale, "Unknown sender", "未知发送方"), style = MaterialTheme.typography.titleMedium)
                    Text(event.redactedSnippet, style = MaterialTheme.typography.bodyLarge)
                    if (event.seededDemoData) DemoBadge(locale)
                }
            }
        }
        item {
            Text(l(locale, "Why it was flagged", "为什么会被标记"), style = MaterialTheme.typography.titleLarge)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                event.signalCodes.forEach { signal -> EvidenceRow(signalLabel(signal, locale)) }
                if (event.signalCodes.isEmpty()) EvidenceRow(l(locale, "No strong warning signs were found", "没有发现明显警告信号"))
            }
        }
        if (related.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = sageCardElevation(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            l(locale, "Personal Scam Memory", "个人诈骗记忆"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            l(locale, "This resembles ${related.size} recent event(s), even if the sender changed.", "这与最近 ${related.size} 条记录相似，即使发送方已经变化。"),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
        item {
            Text(l(locale, "Safer next steps", "更安全的下一步"), style = MaterialTheme.typography.titleLarge)
            EvidenceRow(l(locale, "Do not open links or share verification codes", "不要打开链接或分享验证码"))
            EvidenceRow(l(locale, "Use the organisation's official app or website", "使用机构的官方应用或网站核实"))
            EvidenceRow(l(locale, "Talk to someone you trust before paying", "付款前先与信任的人商量"))
        }
        item {
            Button(onClick = onAsk, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)) {
                Icon(Icons.Default.Chat, null)
                Spacer(Modifier.width(8.dp))
                Text(l(locale, "Ask why", "询问为什么"))
            }
            Text(
                l(locale, "This is a risk warning, not proof that the sender is fraudulent.", "这是风险警告，并非对发送方的诈骗定论。"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun AgentScreen(
    locale: String,
    event: RiskEventEntity?,
    agentState: AgentUiState,
    onBack: () -> Unit,
    onAsk: (String) -> Unit,
    onReset: () -> Unit,
) {
    var question by remember(event?.id) {
        mutableStateOf(l(locale, "Why is this risky, and what should I do next?", "为什么有风险？我下一步应该怎么做？"))
    }
    val uriHandler = LocalUriHandler.current
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BackHeader(l(locale, "Ask SageSense", "询问 SageSense"), onBack) }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = sageCardElevation(),
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    EyeShieldMascot(64.dp)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(
                            l(locale, "Plain-language advisor", "通俗解释 Agent"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            if (event != null) l(locale, "Discussing the selected risk event", "正在分析所选风险事件")
                            else l(locale, "General scam-safety question", "一般防诈问题"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it.take(800) },
                label = { Text(l(locale, "Your question", "你的问题")) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Button(
                onClick = { onAsk(question) },
                enabled = question.isNotBlank() && agentState !is AgentUiState.Loading,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).heightIn(min = 58.dp),
            ) {
                if (agentState is AgentUiState.Loading) {
                    CircularProgressIndicator(
                        Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp,
                    )
                }
                else Text(l(locale, "Explain safely", "安全解释"))
            }
        }
        when (agentState) {
            AgentUiState.Idle -> item { EmptyCard(l(locale, "The Agent only receives redacted context after you press the button.", "只有点击按钮后，Agent 才会收到脱敏后的上下文。")) }
            AgentUiState.Loading -> item { Text(l(locale, "Checking trusted sources…", "正在查询可信来源……"), style = MaterialTheme.typography.bodyLarge) }
            is AgentUiState.Error -> item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = sageCardElevation(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(agentState.message, style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = onReset) { Text(l(locale, "Dismiss", "关闭")) }
                    }
                }
            }
            is AgentUiState.Success -> {
                item {
                    Card(elevation = sageCardElevation()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                Text(l(locale, "Explanation", "解释"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 10.dp))
                            }
                            Text(agentState.answer.answer, style = MaterialTheme.typography.bodyLarge)
                            if (agentState.answer.degraded) {
                                Text(
                                    l(locale, "Offline/degraded answer", "离线/降级回答"),
                                    color = MaterialTheme.sageStatusColors.warning,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                item {
                    Text(l(locale, "Suggested actions", "建议操作"), style = MaterialTheme.typography.titleLarge)
                    agentState.answer.suggestedActions.forEach { action -> EvidenceRow(action.label) }
                }
                item {
                    Text(l(locale, "Sources", "资料来源"), style = MaterialTheme.typography.titleLarge)
                    agentState.answer.citations.forEach { citation ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { uriHandler.openUri(citation.url) },
                            elevation = sageCardElevation(),
                        ) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(citation.title, style = MaterialTheme.typography.titleMedium)
                                    Text(citation.publisher, style = MaterialTheme.typography.bodyMedium)
                                }
                                Icon(Icons.Default.OpenInNew, l(locale, "Open source", "打开来源"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnScreen(locale: String) {
    val uriHandler = LocalUriHandler.current
    val cards = remember {
        listOf(
            LearnCard(
                "phishing",
                "Phishing messages",
                "网络钓鱼信息",
                "Unexpected links can imitate a bank or government service. Open the official app yourself instead.",
                "意外链接可能冒充银行或政府服务。请自行打开官方应用进行核实。",
                "Australian Cyber Security Centre",
                "https://www.cyber.gov.au/protect-yourself/spotting-scams",
            ),
            LearnCard(
                "impersonation",
                "Impersonation and urgency",
                "冒充身份与制造紧迫感",
                "Scammers often combine a trusted identity with pressure to act immediately.",
                "诈骗者经常冒充可信身份，并催促你立即行动。",
                "National Anti-Scam Centre - Scamwatch",
                "https://www.scamwatch.gov.au/stop-check-protect/help-to-spot-and-avoid-scams/methods-scammers-use",
            ),
            LearnCard(
                "recovery",
                "If money or details were sent",
                "如果已经转账或泄露资料",
                "Contact your bank immediately, stop contact, preserve evidence, and use official reporting channels.",
                "立即联系银行、停止沟通、保存证据，并通过官方渠道报告。",
                "Australian Cyber Security Centre",
                "https://www.cyber.gov.au/report-and-recover/recover-from/scams",
            ),
            LearnCard(
                "older-adults",
                "Pause and check",
                "停下来再核实",
                "Unexpected contact, urgency, and unusual payment demands are recurring warning signs.",
                "意外联系、催促行动和异常付款要求是常见警告信号。",
                "United States Federal Trade Commission",
                "https://consumer.ftc.gov/system/files/consumer_ftc_gov/pdf/Guiding%20Principles%20to%20Help%20Older%20Adults%20Spot%20Fraud.pdf",
            ),
        )
    }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(l(locale, "Learn", "学习防诈"), style = MaterialTheme.typography.headlineMedium)
            Text(l(locale, "Short lessons from official sources", "来自官方来源的简短知识"), style = MaterialTheme.typography.bodyLarge)
        }
        items(cards, key = { it.id }) { card ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(card.sourceUrl) },
                elevation = sageCardElevation(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    Text(if (locale == "zh-CN") card.titleZh else card.titleEn, style = MaterialTheme.typography.titleLarge)
                    Text(if (locale == "zh-CN") card.summaryZh else card.summaryEn, style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            card.sourceTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(Icons.Default.OpenInNew, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    locale: String,
    eventsCount: Int,
    permissions: PermissionState,
    themeMode: ThemeMode,
    onLanguage: (String) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onNotificationAccess: () -> Unit,
    onNotificationPermission: () -> Unit,
    onCallRole: () -> Unit,
    onClearHistory: () -> Unit,
    onFaq: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Text(l(locale, "Settings & privacy", "设置与隐私"), style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(elevation = sageCardElevation()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                        Text(l(locale, "Language", "语言"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 10.dp))
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("English", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = locale == "zh-CN", onCheckedChange = { onLanguage(if (it) "zh-CN" else "en-AU") })
                        Text("中文", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onFaq),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = sageCardElevation(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Help, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(l(locale, "FAQ & Safety", "常见问题与安全"), style = MaterialTheme.typography.titleLarge)
                        Text(
                            l(locale, "Learn how the app keeps you safe.", "了解 SageSense 如何保护你。"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
        item {
            Text(l(locale, "Appearance", "外观模式"), style = MaterialTheme.typography.titleLarge)
            Card(elevation = sageCardElevation()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            l(locale, "Theme", "主题"),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                    ThemeMode.entries.forEach { option ->
                        val label = when (option) {
                            ThemeMode.SYSTEM -> l(locale, "System", "跟随系统")
                            ThemeMode.LIGHT -> l(locale, "Light", "白天")
                            ThemeMode.DARK -> l(locale, "Dark", "夜晚")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .clickable { onThemeMode(option) }
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = themeMode == option, onClick = { onThemeMode(option) })
                            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
        item {
            Text(l(locale, "Protection access", "防护授权"), style = MaterialTheme.typography.titleLarge)
            StatusRow(Icons.Default.Notifications, l(locale, "Notification access", "通知读取权限"), permissions.notificationAccess, onNotificationAccess)
            StatusRow(Icons.Default.Warning, l(locale, "Visible warning permission", "警告显示权限"), permissions.notificationPosting, onNotificationPermission)
            if (permissions.callScreeningAvailable) {
                StatusRow(Icons.Default.Call, l(locale, "Call screening role", "来电筛查角色"), permissions.callScreening, onCallRole)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = sageCardElevation(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        l(locale, "Privacy by default", "默认保护隐私"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(l(locale, "• Risk checks run locally\n• Sensitive number patterns are redacted\n• Agent requests contain at most 10 recent summaries\n• The backend does not store request bodies", "• 风险检测在本机运行\n• 敏感号码模式会被脱敏\n• Agent 最多接收 10 条近期摘要\n• 后端不保存请求正文"), style = MaterialTheme.typography.bodyLarge)
                    Text(l(locale, "Non-demo history is retained for 30 days.", "非演示历史默认保留 30 天。"), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(
                    l(locale, "Delete all history ($eventsCount)", "删除全部历史（$eventsCount）"),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item {
            Card(elevation = sageCardElevation()) {
                Column(Modifier.padding(18.dp)) {
                    Text(l(locale, "Agent service", "Agent 服务"), style = MaterialTheme.typography.titleMedium)
                    Text("DeepSeek V4 Flash · server-side key · 10 s timeout", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(l(locale, "Delete risk history?", "删除风险历史？")) },
            text = { Text(l(locale, "This removes stored events from this phone. Seeded Watchlist fixtures remain.", "这会删除手机上的风险事件。演示用观察名单仍会保留。")) },
            confirmButton = {
                TextButton(onClick = { onClearHistory(); confirmDelete = false }) {
                    Text(l(locale, "Delete", "删除"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(l(locale, "Cancel", "取消")) } },
        )
    }
}

@Composable
private fun BackHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(30.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun EvidenceRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.sageStatusColors.success, modifier = Modifier.size(26.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = sageCardElevation(),
    ) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(20.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun RiskBadge(level: RiskLevel, locale: String) {
    Box(Modifier.clip(RoundedCornerShape(30.dp)).background(riskColour(level)).padding(horizontal = 13.dp, vertical = 7.dp)) {
        Text(riskLabel(level, locale), color = riskOnColour(level), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun DemoBadge(locale: String) {
    AssistChip(
        onClick = {},
        label = { Text(l(locale, "Seeded demo data", "演示测试数据")) },
        leadingIcon = { Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp)) },
    )
}

@Composable
internal fun EyeShieldMascot(size: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(R.drawable.sagesense_anti_scam_mascot),
        contentDescription = "SageSense anti-scam shield companion",
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun riskColour(level: RiskLevel): Color = when (level) {
    RiskLevel.HIGH -> MaterialTheme.colorScheme.error
    RiskLevel.MEDIUM -> MaterialTheme.sageStatusColors.warning
    RiskLevel.LOW -> MaterialTheme.sageStatusColors.success
}

@Composable
private fun riskOnColour(level: RiskLevel): Color = when (level) {
    RiskLevel.HIGH -> MaterialTheme.colorScheme.onError
    RiskLevel.MEDIUM -> MaterialTheme.sageStatusColors.onWarning
    RiskLevel.LOW -> MaterialTheme.sageStatusColors.onSuccess
}

private fun riskLabel(level: RiskLevel, locale: String): String = when (level) {
    RiskLevel.HIGH -> l(locale, "High", "高")
    RiskLevel.MEDIUM -> l(locale, "Medium", "中")
    RiskLevel.LOW -> l(locale, "Low", "低")
}

private fun signalLabel(signal: String, locale: String): String = when (signal) {
    "URGENCY" -> l(locale, "Pressure to act immediately", "催促立即行动")
    "PAYMENT_REQUEST" -> l(locale, "Requests money or an unusual payment", "要求付款或使用异常付款方式")
    "CREDENTIAL_REQUEST" -> l(locale, "Requests private account information", "索取账户隐私信息")
    "OTP_REQUEST" -> l(locale, "Requests a one-time verification code", "索取一次性验证码")
    "SUSPICIOUS_URL" -> l(locale, "Contains a suspicious link", "包含可疑链接")
    "MISSPELLED_DOMAIN" -> l(locale, "Domain resembles a trusted brand", "域名疑似模仿可信品牌")
    "BRAND_IMPERSONATION" -> l(locale, "Claims a brand but links elsewhere", "声称来自品牌但链接并非官方域名")
    "WATCHLIST_MATCH" -> l(locale, "Matches the local Risk Watchlist", "命中本地风险观察名单")
    else -> signal.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun formatTime(value: Long): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(value))
