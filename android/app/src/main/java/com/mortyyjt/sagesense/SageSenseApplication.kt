package com.mortyyjt.sagesense

import android.app.Application
import android.util.Log
import com.mortyyjt.sagesense.service.AlertNotifier
import kotlinx.coroutines.CancellationException
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
            container.riskRepository.prune()
            if (container.preferences.historyPrivacyVersion.first() < HISTORY_PRIVACY_VERSION) {
                try {
                    container.riskRepository.minimiseExistingHistory()
                    container.preferences.markHistoryPrivacyVersion(HISTORY_PRIVACY_VERSION)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    // Retried on next launch. Never log row contents or an
                    // exception message that might include database values.
                    Log.w(TAG, "History privacy migration failed: ${error.javaClass.simpleName}")
                }
            }
            container.riskRepository.seedDemoData()
        }
    }

    /**
     * Runs short application work that must survive a bound Android service
     * being released immediately after it returns its system response.
     */
    fun launchBackground(block: suspend CoroutineScope.() -> Unit): Job =
        applicationScope.launch(block = block)

    private companion object {
        const val TAG = "SageSenseApplication"
        const val HISTORY_PRIVACY_VERSION = 1
    }
}
