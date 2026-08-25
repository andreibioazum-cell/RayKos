package io.github.romanvht.byedpi.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import io.github.romanvht.byedpi.data.AppStatus
import io.github.romanvht.byedpi.data.START_ACTION
import io.github.romanvht.byedpi.data.STOP_ACTION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ServiceManager {
    private val TAG = ServiceManager::class.java.simpleName
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(context: Context) {
        Log.i(TAG, "Starting VPN")
        val intent = Intent(context, ByeDpiVpnService::class.java)
        intent.action = START_ACTION
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
        Log.i(TAG, "Stopping VPN")
        val intent = Intent(context, ByeDpiVpnService::class.java)
        intent.action = STOP_ACTION
        ContextCompat.startForegroundService(context, intent)
    }

    fun restart(context: Context) {
        if (appStatus == AppStatus.Running) {
            stop(context)
            scope.launch {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < 3000L) {
                    if (appStatus == AppStatus.Halted) break
                    delay(100)
                }
                start(context)
            }
        }
    }
}
