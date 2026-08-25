package io.github.romanvht.byedpi.utility

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.edit
import io.github.romanvht.byedpi.R
import io.github.romanvht.byedpi.activities.ToggleActivity
import java.util.UUID

object ShortcutUtils {
    const val TOKEN_EXTRA = "shortcut_token"
    private const val TOKEN_PREFERENCE = "shortcut_token"

    fun update(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val prefs = context.getPreferences()
            val token = prefs.getString(TOKEN_PREFERENCE, null) ?: UUID.randomUUID().toString().also {
                prefs.edit { putString(TOKEN_PREFERENCE, it) }
            }

            val toggleIntent = Intent(context, ToggleActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(TOKEN_EXTRA, token)
            }

            val toggleShortcut = ShortcutInfo.Builder(context, "toggle_service")
                .setShortLabel(context.getString(R.string.toggle_connect))
                .setLongLabel(context.getString(R.string.toggle_connect))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_toggle))
                .setIntent(toggleIntent)
                .build()

            shortcutManager.dynamicShortcuts = mutableListOf(toggleShortcut)
        }
    }
}
