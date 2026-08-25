package io.github.romanvht.byedpi.activities

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import io.github.romanvht.byedpi.data.AppStatus
import io.github.romanvht.byedpi.services.ServiceManager
import io.github.romanvht.byedpi.services.appStatus
import io.github.romanvht.byedpi.utility.ShortcutUtils
import io.github.romanvht.byedpi.utility.getPreferences

class ToggleActivity : Activity() {

    companion object {
        private const val TAG = "ToggleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTrustedShortcut()) {
            Log.w(TAG, "Rejected untrusted shortcut invocation")
            finish()
            return
        }

        when (appStatus) {
            AppStatus.Halted -> startService()
            AppStatus.Running -> stopService()
        }

        finish()
    }

    private fun isTrustedShortcut(): Boolean {
        val expected = getPreferences().getString(ShortcutUtils.TOKEN_EXTRA, null) ?: return false
        val supplied = intent.getStringExtra(ShortcutUtils.TOKEN_EXTRA) ?: return false
        return supplied == expected
    }

    private fun startService() {
        if (VpnService.prepare(this) != null) {
            Log.w(TAG, "VPN permission not granted")
            return
        }

        ServiceManager.start(this)
        Log.i(TAG, "Toggle service start")
    }

    private fun stopService() {
        ServiceManager.stop(this)
        Log.i(TAG, "Toggle service stop")
    }
}
