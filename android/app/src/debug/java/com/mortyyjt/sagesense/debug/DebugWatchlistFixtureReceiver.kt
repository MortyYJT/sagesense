package com.mortyyjt.sagesense.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mortyyjt.sagesense.SageSenseApplication
import com.mortyyjt.sagesense.data.TemporaryCallFixture

/**
 * ADB-only physical-device QA hook included in debug builds, never release.
 * Component access is also restricted by android.permission.DUMP in the debug
 * manifest, which the Android shell holds and ordinary third-party apps do not.
 */
class DebugWatchlistFixtureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as? SageSenseApplication
        if (app == null) {
            pendingResult.finish()
            return
        }
        app.launchBackground {
            try {
                val rawPhone = intent.getStringExtra(EXTRA_PHONE).orEmpty()
                val fixture = TemporaryCallFixture.create(rawPhone)
                when (intent.action) {
                    ACTION_ADD -> {
                        if (fixture == null) {
                            Log.w(TAG, "Rejected invalid temporary caller fixture")
                        } else {
                            app.container.database.watchlistDao().upsertAll(listOf(fixture))
                            Log.i(TAG, "Added temporary caller fixture ${fixture.value}")
                        }
                    }
                    ACTION_REMOVE -> {
                        val id = fixture?.id ?: TemporaryCallFixture.idForRawPhone(rawPhone)
                        if (id == null || !TemporaryCallFixture.isTemporaryId(id)) {
                            Log.w(TAG, "Rejected invalid temporary caller removal")
                        } else {
                            app.container.database.watchlistDao().deleteById(id)
                            Log.i(TAG, "Removed temporary caller fixture")
                        }
                    }
                    else -> Log.w(TAG, "Ignored unknown debug fixture action")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "SageSenseDebugFixture"
        const val EXTRA_PHONE = "phone"
        const val ACTION_ADD = "com.mortyyjt.sagesense.debug.ADD_TEMPORARY_CALLER"
        const val ACTION_REMOVE = "com.mortyyjt.sagesense.debug.REMOVE_TEMPORARY_CALLER"
    }
}
