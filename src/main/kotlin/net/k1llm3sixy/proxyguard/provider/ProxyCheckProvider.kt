package net.k1llm3sixy.proxyguard.provider

import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI

class ProxyCheckProvider : BaseProvider()
{
    private data class Response(
        val detections: Detections,
    )

    private data class Detections(
        val vpn: Boolean,
        val proxy: Boolean,
    )

    override val provider = Provider.PROXY_CHECK
    override val key: String
        get() = CONFIG.getString(Route.from("proxycheck-api-key"))

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
            )?.detections ?: return@getResult false

            data.vpn || data.proxy
        }
    }
}
