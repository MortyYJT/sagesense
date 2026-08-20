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
import com.mortyyjt.sagesense.ui.SageSenseApp
import com.mortyyjt.sagesense.ui.SageSenseViewModel
import com.mortyyjt.sagesense.ui.theme.SageSenseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SageSenseViewModel by viewModels {
        SageSenseViewModel.factory((application as SageSenseApplication).container)
    }
    private var pendingEventId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingEventId = intent.eventId()
        setContent {
            SageSenseTheme {
                SageSenseApp(
                    viewModel = viewModel,
                    initialEventId = pendingEventId,
                    onDeepLinkConsumed = { pendingEventId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingEventId = intent.eventId()
    }

    private fun Intent.eventId(): String? = data
        ?.takeIf { it.scheme == "sagesense" && it.host == "event" }
        ?.pathSegments
        ?.firstOrNull()
}
