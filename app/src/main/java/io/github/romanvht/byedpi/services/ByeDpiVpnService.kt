package io.github.romanvht.byedpi.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.lifecycleScope
import io.github.romanvht.byedpi.R
import io.github.romanvht.byedpi.activities.MainActivity
import io.github.romanvht.byedpi.core.ByeDpiProxy
import io.github.romanvht.byedpi.core.ByeDpiProxyPreferences
import io.github.romanvht.byedpi.core.TProxyService
import io.github.romanvht.byedpi.data.AppStatus
import io.github.romanvht.byedpi.data.FAILED_BROADCAST
import io.github.romanvht.byedpi.data.PAUSE_ACTION
import io.github.romanvht.byedpi.data.RESUME_ACTION
import io.github.romanvht.byedpi.data.ServiceStatus
import io.github.romanvht.byedpi.data.START_ACTION
import io.github.romanvht.byedpi.data.STARTED_BROADCAST
import io.github.romanvht.byedpi.data.STOP_ACTION
import io.github.romanvht.byedpi.data.STOPPED_BROADCAST
import io.github.romanvht.byedpi.data.RayKosConfig
import io.github.romanvht.byedpi.utility.createConnectionNotification
import io.github.romanvht.byedpi.utility.createPauseNotification
import io.github.romanvht.byedpi.utility.registerNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class ByeDpiVpnService : LifecycleVpnService() {
    private val byeDpiProxy = ByeDpiProxy()
    private var proxyJob: Job? = null
    private var tunFd: ParcelFileDescriptor? = null
    private val mutex = Mutex()

    companion object {
        private val TAG: String = ByeDpiVpnService::class.java.simpleName
        private const val FOREGROUND_SERVICE_ID: Int = 1
        private const val PAUSE_NOTIFICATION_ID: Int = 3
        private const val NOTIFICATION_CHANNEL_ID: String = "ByeDPIVpn"

        private var status: ServiceStatus = ServiceStatus.Disconnected
    }

    override fun onCreate() {
        super.onCreate()
        registerNotificationChannel(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.vpn_channel_name,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        tunFd?.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground()

        return when (val action = intent?.action) {
            START_ACTION -> {
                lifecycleScope.launch {
                    start()
                }
                START_STICKY
            }

            STOP_ACTION -> {
                lifecycleScope.launch {
                    stop()
                }
                START_NOT_STICKY
            }

            RESUME_ACTION -> {
                lifecycleScope.launch {
                    if (prepare(this@ByeDpiVpnService) == null) {
                        start()
                    }
                }
                START_STICKY
            }

            PAUSE_ACTION -> {
                lifecycleScope.launch {
                    stop()
                    createNotificationPause()
                }
                START_NOT_STICKY
            }

            SERVICE_INTERFACE -> {
                Log.i(TAG, "Started by Android")

                lifecycleScope.launch {
                    start()
                }

                START_STICKY
            }

            else -> {
                Log.w(TAG, "Unknown action: $action")
                START_NOT_STICKY
            }
        }
    }

    override fun onRevoke() {
        Log.i(TAG, "VPN revoked")
        lifecycleScope.launch { stop() }
    }

    private suspend fun start() {
        Log.i(TAG, "Starting")

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(PAUSE_NOTIFICATION_ID)

        if (status == ServiceStatus.Connected) {
            Log.w(TAG, "VPN already connected")
            updateStatus(ServiceStatus.Connected)
            return
        }

        try {
            mutex.withLock {
                startProxy()
                startTun2Socks()
                updateStatus(ServiceStatus.Connected)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN", e)
            updateStatus(ServiceStatus.Failed)
            stop()
        }
    }

    private fun startForeground() {
        val notification: Notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_SERVICE_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
            )
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    private suspend fun stop() {
        Log.i(TAG, "Stopping")

        if (status != ServiceStatus.Connected) {
            Log.w(TAG, "VPN not connected")
            updateStatus(ServiceStatus.Disconnected)
            return
        }

        mutex.withLock {
            try {
                withContext(Dispatchers.IO) {
                    stopProxy()
                    stopTun2Socks()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop VPN", e)
            }
        }

        updateStatus(ServiceStatus.Disconnected)
        stopSelf()
    }

    private fun startProxy() {
        Log.i(TAG, "Starting proxy")

        if (proxyJob != null) {
            Log.w(TAG, "Proxy fields not null")
            throw IllegalStateException("Proxy fields not null")
        }

        val preferences = ByeDpiProxyPreferences()

        proxyJob = lifecycleScope.launch(Dispatchers.IO) {
            val code = byeDpiProxy.startProxy(preferences)

            delay(500)

            if (code != 0) {
                Log.e(TAG, "Proxy stopped with code $code")
                updateStatus(ServiceStatus.Failed)
                stopTun2Socks()
                stopSelf()
            }
        }

        Log.i(TAG, "Proxy started")
    }

    private suspend fun stopProxy() {
        Log.i(TAG, "Stopping proxy")

        if (status == ServiceStatus.Disconnected) {
            Log.w(TAG, "Proxy already disconnected")
            return
        }

        try {
            byeDpiProxy.stopProxy()
            proxyJob?.cancel()

            val completed = withTimeoutOrNull(2000) {
                proxyJob?.join()
                true
            }

            if (completed == null) {
                Log.w(TAG, "proxy not finish in time, cancelling...")
                byeDpiProxy.jniForceClose()
            }

            proxyJob = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close proxyJob", e)
        }

        Log.i(TAG, "Proxy stopped")
    }

    private fun startTun2Socks() {
        Log.i(TAG, "Starting tun2socks")

        if (tunFd != null) {
            Log.w(TAG, "VPN field not null")
            throw IllegalStateException("VPN field not null")
        }

        val tun2socksConfig = buildString {
            appendLine("tunnel:")
            appendLine("  mtu: 8500")

            appendLine("misc:")
            appendLine("  task-stack-size: 81920")

            appendLine("socks5:")
            appendLine("  address: ${RayKosConfig.PROXY_IP}")
            appendLine("  port: ${RayKosConfig.PROXY_PORT}")
            appendLine("  udp: udp")
        }

        val configPath = try {
            File.createTempFile("config", "tmp", cacheDir).apply {
                writeText(tun2socksConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create config file", e)
            throw e
        }

        val fd = createBuilder(RayKosConfig.DNS_IP, RayKosConfig.IPV6_ENABLED).establish()
            ?: throw IllegalStateException("VPN connection failed")

        this.tunFd = fd

        TProxyService.TProxyStartService(configPath.absolutePath, fd.fd)

        Log.i(TAG, "Tun2Socks started. ip: ${RayKosConfig.PROXY_IP} port: ${RayKosConfig.PROXY_PORT}")
    }

    private fun stopTun2Socks() {
        Log.i(TAG, "Stopping tun2socks")

        if (tunFd == null) {
            Log.w(TAG, "VPN field is null, skipping")
            return
        }

        try {
            TProxyService.TProxyStopService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop TProxyService", e)
        }

        try {
            File(cacheDir, "config.tmp").delete()
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to delete config file", e)
        }

        try {
            tunFd?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close tunFd", e)
        } finally {
            tunFd = null
        }

        Log.i(TAG, "Tun2Socks stopped")
    }

    private fun updateStatus(newStatus: ServiceStatus) {
        Log.d(TAG, "VPN status changed from $status to $newStatus")

        status = newStatus

        setStatus(
            when (newStatus) {
                ServiceStatus.Connected -> AppStatus.Running
                ServiceStatus.Disconnected,
                ServiceStatus.Failed -> {
                    proxyJob = null
                    AppStatus.Halted
                }
            }
        )

        val intent = Intent(
            when (newStatus) {
                ServiceStatus.Connected -> STARTED_BROADCAST
                ServiceStatus.Disconnected -> STOPPED_BROADCAST
                ServiceStatus.Failed -> FAILED_BROADCAST
            }
        )
        sendBroadcast(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            QuickTileService.updateTile()
        }
    }

    private fun createNotification(): Notification =
        createConnectionNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.vpn_notification_content,
            ByeDpiVpnService::class.java,
        )

    private fun createNotificationPause() {
        val notification = createPauseNotification(
            this,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_title,
            R.string.service_paused_text,
            ByeDpiVpnService::class.java,
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(PAUSE_NOTIFICATION_ID, notification)
    }

    private fun createBuilder(dns: String, ipv6: Boolean): Builder {
        Log.d(TAG, "DNS: $dns")
        val builder = Builder()
        builder.setSession("RayKos")
        builder.setConfigureIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
        )

        builder.addAddress("10.10.10.10", 32)
            .addRoute("0.0.0.0", 0)

        if (ipv6) {
            builder.addAddress("fd00::1", 128)
                .addRoute("::", 0)
        }

        if (dns.isNotBlank()) {
            builder.addDnsServer(dns)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        return builder
    }
}
