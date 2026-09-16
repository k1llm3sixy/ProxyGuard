package net.k1llm3sixy.proxyguard.provider

import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG

class VpnApiProvider : BaseProvider()
{
    private data class Response(
        val security: Security,
    )

    private data class Security(
        val vpn: Boolean,
        val proxy: Boolean,
    )

    override val provider = Provider.VPN_API
    override val key: String
        get() = CONFIG.get(ConfigRoute.VPN_API_KEY)

    override suspend fun proxy(ip: String): Reason
    {
        val uri = provider.url.format(
            ip,
            key
        )

        return getResult(uri) {
            val data = gson.fromJson(
                it.body(),
                Response::class.java
            )?.security ?: return@getResult Reason.EMPTY

            check(
                data.vpn,
                data.proxy
            )
        }
    }
}
