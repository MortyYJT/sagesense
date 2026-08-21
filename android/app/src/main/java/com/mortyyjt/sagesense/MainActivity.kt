package com.mortyyjt.sagesense

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mortyyjt.sagesense.ui.SageSenseApp
import com.mortyyjt.sagesense.ui.SageSenseViewModel
import com.mortyyjt.sagesense.ui.theme.SageSenseTheme
import com.mortyyjt.sagesense.service.RiskOverlayController

class MainActivity : ComponentActivity() {
    private val viewModel: SageSenseViewModel by viewModels {
        SageSenseViewModel.factory((application as SageSenseApplication).container)
    }
    private var pendingEventId by mutableStateOf<String?>(null)
    private var pendingCognitivePauseEventId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updatePendingIntent(intent)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SageSenseTheme(themeMode = state.themeMode) {
                SageSenseApp(
                    viewModel = viewModel,
                    initialEventId = pendingEventId,
                    initialCognitivePauseEventId = pendingCognitivePauseEventId,
                    onDeepLinkConsumed = {
                        pendingEventId = null
                        pendingCognitivePauseEventId = null
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updatePendingIntent(intent)
    }

    private fun updatePendingIntent(intent: Intent) {
        val eventId = intent.eventId()
        if (intent.getBooleanExtra(RiskOverlayController.EXTRA_SHOW_COGNITIVE_PAUSE, false)) {
            pendingCognitivePauseEventId = eventId
            pendingEventId = null
        } else {
            pendingEventId = eventId
            pendingCognitivePauseEventId = null
        }
    }

    private fun Intent.eventId(): String? = data
        ?.takeIf { it.scheme == "sagesense" && it.host == "event" }
        ?.pathSegments
        ?.firstOrNull()
}
