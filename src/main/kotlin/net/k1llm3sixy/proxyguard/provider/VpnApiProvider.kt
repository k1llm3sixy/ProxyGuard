package net.k1llm3sixy.proxyguard.provider

import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI

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
        get() = CONFIG.getString(Route.from("vpn-api-key"))

    override suspend fun proxy(ip: String): Boolean
    {
        val uri = provider.url.format(
            ip,
            key
        )

        return getResult(URI.create(uri)) {
            val data = gson.fromJson(
                it.body(),
                Response::class.java
            )?.security ?: return@getResult false

            data.vpn || data.proxy
        }
    }
}
