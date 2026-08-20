package com.mortyyjt.sagesense.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.mortyyjt.sagesense.SageSenseApplication
import com.mortyyjt.sagesense.risk.RiskAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SageCallScreeningService : CallScreeningService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        val number = callDetails.handle?.schemeSpecificPart
        if (number.isNullOrBlank()) {
            respondToCall(callDetails, response)
            return
        }

        val app = application as SageSenseApplication
        serviceScope.launch {
            val match = withTimeoutOrNull(3_000) {
                app.container.database.watchlistDao().findNormalised(RiskAnalyzer.normaliseEntity(number))
            }
            respondToCall(callDetails, response)
            if (match != null) {
                val event = app.container.riskRepository.analyseAndStore(
                    sourceType = "call",
                    sender = number,
                    text = "Incoming caller matched the local Risk Watchlist: ${match.reasonEn}",
                    seededDemoData = match.seededDemoData,
                )
                AlertNotifier.showRisk(this@SageCallScreeningService, event, "This call may be unsafe")
            }
        }
    }
}
