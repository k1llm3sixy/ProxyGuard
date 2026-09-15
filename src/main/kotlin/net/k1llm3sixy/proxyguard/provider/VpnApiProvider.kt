package net.k1llm3sixy.proxyguard.provider

import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI
import java.net.http.HttpResponse
import kotlin.time.Duration.Companion.seconds

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

    override suspend fun proxy(ip: String): Reason?
    {
        val uri = provider.url.format(
            ip,
            key
        )

        return withTimeoutOrNull(5.seconds) {
            runCatching {
                val response = client.sendAsync(
                    createRequest(URI.create(uri)),
                    HttpResponse.BodyHandlers.ofString()
                ).await()

                if (response.statusCode() == 200)
                {
                    val data = gson.fromJson(
                        response.body(),
                        Response::class.java
                    )?.security ?: return@runCatching null

                    check(
                        data.vpn,
                        data.proxy
                    )
                }
                else null
            }.getOrNull()
        }
    }
}
