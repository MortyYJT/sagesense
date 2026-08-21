package com.mortyyjt.sagesense

import android.app.Application
import com.mortyyjt.sagesense.service.AlertNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
            AlertNotifier.createChannels(this@SageSenseApplication, container.preferences.language.first())
            container.riskRepository.seedDemoData()
            container.riskRepository.prune()
        }
    }

    /**
     * Runs short application work that must survive a bound Android service
     * being released immediately after it returns its system response.
     */
    fun launchBackground(block: suspend CoroutineScope.() -> Unit): Job =
        applicationScope.launch(block = block)
}
