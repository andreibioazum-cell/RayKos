package io.github.romanvht.byedpi.data

/**
 * RayKos has no settings and no editors: the best bypass parameters are
 * baked in and the proxy always starts with this single configuration.
 */
object RayKosConfig {
    const val PROXY_IP = "127.0.0.1"
    const val PROXY_PORT = 1080

    const val DNS_IP = "1.1.1.1"
    const val IPV6_ENABLED = false

    /**
     * Starts with light TCP tweaks and automatically steps through two
     * increasingly aggressive fallbacks when ByeDPI detects a reset,
     * redirect, or broken TLS reply. The selected group is cached by
     * ByeDPI, so blocked hosts do not pay the detection delay on every
     * connection. UDP gets one harmless fake packet from the start.
     */
    const val CMD_ARGS =
        "-a1 -T3 -At,r,s -Lo,s -o1 -r-5+se -At,s -d1 -s1+s -d3+s -f-1 -Qr -n www.iana.org"
}
