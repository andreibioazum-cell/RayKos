package io.github.romanvht.byedpi.core

import io.github.romanvht.byedpi.data.RayKosConfig
import io.github.romanvht.byedpi.utility.shellSplit

/**
 * RayKos exposes no settings: the ByeDPI proxy always starts with the
 * built-in bypass parameters from [RayKosConfig].
 */
class ByeDpiProxyPreferences(val args: Array<String>) {
    constructor() : this(buildArgs())

    companion object {
        private fun buildArgs(): Array<String> {
            val blacklist = setOf("--help", "--version", "-h", "-v")
            val prefix = "--ip ${RayKosConfig.PROXY_IP} --port ${RayKosConfig.PROXY_PORT} "
            val splitArgs = shellSplit(prefix + RayKosConfig.CMD_ARGS).filter { it !in blacklist }

            return (listOf("ciadpi") + splitArgs).toTypedArray()
        }
    }
}
