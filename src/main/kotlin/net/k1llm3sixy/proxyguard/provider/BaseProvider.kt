package net.k1llm3sixy.proxyguard.provider

import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Config.CONFIG
import java.net.http.HttpClient
import java.time.Duration

// TODO: СДЕЛАТЬ ОБРАБОТКУ ОШИБОК, ФОЛЛБЕКИ И ПРОЧЕЕ!!
abstract class BaseProvider
{
    companion object
    {
        fun getProvider() = when (CONFIG.getEnum(
            Route.from("provider"),
            Provider::class.java
        ))
        {
            Provider.IP_API      -> IpApiProvider()
            Provider.VPN_API     -> VpnApiProvider()
            Provider.PROXY_CHECK -> ProxyCheckProvider()
        }
    }

    protected val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    abstract val provider: Provider

    abstract suspend fun proxy(ip: String): Boolean
}