package com.mortyyjt.sagesense

import android.content.Context
import com.mortyyjt.sagesense.data.PreferencesStore
import com.mortyyjt.sagesense.data.RiskEventRepository
import com.mortyyjt.sagesense.data.SageSenseDatabase
import com.mortyyjt.sagesense.network.AgentClient
import com.mortyyjt.sagesense.risk.RiskAnalyzer
import com.mortyyjt.sagesense.risk.RiskWeights

class AppContainer(context: Context) {
    private val riskWeights = context.assets.open("risk_weights.json")
        .bufferedReader()
        .use { RiskWeights.fromJson(it.readText()) }
    val database = SageSenseDatabase.get(context)
    val preferences = PreferencesStore(context)
    val riskRepository = RiskEventRepository(
        eventDao = database.riskEventDao(),
        watchlistDao = database.watchlistDao(),
        analyzer = RiskAnalyzer(riskWeights),
    )
    val agentClient = AgentClient()
}
