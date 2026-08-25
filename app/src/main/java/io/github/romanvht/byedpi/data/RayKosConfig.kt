package io.github.romanvht.byedpi.data

/**
 * RayKos has no settings and no editors: the best bypass parameters are
 * baked in and the proxy always starts with this single configuration.
 */
object RayKosConfig {
    const val PROXY_IP = "127.0.0.1"
    const val PROXY_PORT = 1080

    const val DNS_IP = "8.8.8.8"
    const val IPV6_ENABLED = false

    /**
     * Drop QUIC so YouTube/googlevideo fall back to TCP (DPI often kills UDP).
     * Auto-detect with split/disorder/fake TLS, then a stronger split fallback.
     */
    const val CMD_ARGS =
        "-q1 -a1 -T5 -At,r,s -s1+s -d1+s -f-1 -n www.google.com -An -s1 -q1"
}
