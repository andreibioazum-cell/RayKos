package io.github.romanvht.byedpi.activities

import android.app.Activity
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.edit
import io.github.romanvht.byedpi.data.AppStatus
import io.github.romanvht.byedpi.data.Mode
import io.github.romanvht.byedpi.services.ServiceManager
import io.github.romanvht.byedpi.services.appStatus
import io.github.romanvht.byedpi.utility.HistoryUtils
import io.github.romanvht.byedpi.utility.ShortcutUtils
import io.github.romanvht.byedpi.utility.getCmdArgs
import io.github.romanvht.byedpi.utility.getPreferences
import io.github.romanvht.byedpi.utility.mode

class ToggleActivity : Activity() {

    companion object {
        private const val TAG = "ToggleServiceActivity"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getPreferences()
        if (!isTrustedShortcut()) {
            Log.w(TAG, "Rejected untrusted shortcut invocation")
            finish()
            return
        }

        val strategy = intent.getStringExtra("strategy")
        val updated = updateStrategy(strategy)

        toggleService(updated)
        finish()
    }

    private fun isTrustedShortcut(): Boolean {
        val expected = prefs.getString(ShortcutUtils.TOKEN_EXTRA, null) ?: return false
        val supplied = intent.getStringExtra(ShortcutUtils.TOKEN_EXTRA) ?: return false
        return supplied == expected
    }

    private fun startService() {
        val mode = prefs.mode()

        if (mode == Mode.VPN && VpnService.prepare(this) != null) {
            return
        }

        ServiceManager.start(this, mode)
        Log.i(TAG, "Toggle service start")
    }

    private fun restartService() {
        val mode = prefs.mode()

        if (mode == Mode.VPN && VpnService.prepare(this) != null) {
            return
        }

        ServiceManager.restart(this, mode)
        Log.i(TAG, "Toggle service start")
    }

    private fun stopService() {
        ServiceManager.stop(this)
        Log.i(TAG, "Toggle service stop")
    }

    private fun toggleService(restart: Boolean) {
        val (status) = appStatus
        when (status) {
            AppStatus.Halted -> {
                startService()
            }
            AppStatus.Running -> {
                if (restart) {
                    restartService()
                } else {
                    stopService()
                }
            }
        }
    }

    private fun updateStrategy(strategy: String?): Boolean {
        if (strategy == null) return false

        // This activity has to stay exported because Android launchers invoke the
        // dynamic shortcuts from another process. Never accept an arbitrary command
        // from that exported entry point: only currently pinned shortcut strategies
        // are valid.
        val isPinnedShortcut = HistoryUtils(this).getPinnedHistory().any { it.text == strategy }
        if (!isPinnedShortcut) {
            Log.w(TAG, "Rejected strategy that is not a pinned shortcut")
            return false
        }

        val current = prefs.getCmdArgs()
        if (strategy != current) {
            prefs.edit(commit = true) { putString("byedpi_cmd_args", strategy) }
            Log.i(TAG, "Strategy updated from a pinned shortcut")
            return true
        }
        return false
    }
}
