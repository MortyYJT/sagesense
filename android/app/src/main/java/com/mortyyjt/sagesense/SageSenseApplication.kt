package com.mortyyjt.sagesense

import android.app.Application
import com.mortyyjt.sagesense.service.AlertNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SageSenseApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        AlertNotifier.createChannels(this)
        applicationScope.launch {
            container.riskRepository.seedDemoData()
            container.riskRepository.prune()
        }
    }
}
