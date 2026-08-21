package com.mortyyjt.sagesense.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.mortyyjt.sagesense.SageSenseApplication
import com.mortyyjt.sagesense.risk.RiskAnalyzer
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first

class SageCallScreeningService : CallScreeningService() {
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

        // Always allow the call immediately. The warning is informational and must
        // never make the telecom screening path wait for storage or network work.
        respondToCall(callDetails, response)

        val app = application as SageSenseApplication
        // Telecom normally releases this bound service as soon as the allow
        // response above arrives. Application-scoped, bounded work prevents the
        // local Watchlist warning from being cancelled during that unbind.
        app.launchBackground {
            val match = withTimeoutOrNull(3_000) {
                app.container.database.watchlistDao().findNormalised(RiskAnalyzer.normaliseEntity(number))
            }
            if (match != null) {
                val event = app.container.riskRepository.analyseAndStore(
                    sourceType = "call",
                    sender = number,
                    text = "Incoming caller matched the local Risk Watchlist: ${match.reasonEn}",
                    seededDemoData = match.seededDemoData,
                )
                val locale = app.container.preferences.language.first()
                AlertNotifier.showRisk(app, event, locale = locale)
            }
        }
    }
}
