package io.github.romanvht.byedpi.utility

import android.content.Context
import android.content.SharedPreferences

// RayKos keeps no user-configurable options. The only data stored here are
// internal flags (shortcut token, battery-optimization request).
fun Context.getPreferences(): SharedPreferences =
    getSharedPreferences("raykos", Context.MODE_PRIVATE)
