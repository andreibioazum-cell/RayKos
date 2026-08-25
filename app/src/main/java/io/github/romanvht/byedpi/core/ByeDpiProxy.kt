package io.github.romanvht.byedpi.core

class ByeDpiProxy {
    companion object {
        init {
            System.loadLibrary("byedpi")
        }
    }

    fun startProxy(preferences: ByeDpiProxyPreferences): Int {
        return jniStartProxy(preferences.args)
    }

    fun stopProxy(): Int {
        return jniStopProxy()
    }

    private external fun jniStartProxy(args: Array<String>): Int
    private external fun jniStopProxy(): Int
    external fun jniForceClose(): Int
}
