package net.k1llm3sixy.proxyguard.provider

import com.google.gson.Gson
import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Check
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.time.Duration

enum class Reason
{
    VPN,
    PROXY
}

abstract class BaseProvider
{
    companion object
    {
        fun provider() = when (CONFIG.getEnum(
            Route.from("provider"),
            Provider::class.java
        ))
        {
            Provider.VPN_API     -> VpnApiProvider()
            Provider.PROXY_CHECK -> ProxyCheckProvider()
        }
    }

    abstract val provider: Provider
    abstract val key: String

    protected fun check(vpn: Boolean, proxy: Boolean) = when (Storage.getCheck())
    {
        Check.ALL   -> when
        {
            vpn   -> Reason.VPN
            proxy -> Reason.PROXY
            else  -> null
        }

        Check.VPN   -> if (vpn) Reason.VPN else null
        Check.PROXY -> if (proxy) Reason.PROXY else null
    }

    protected val gson = Gson()

    protected val client: HttpClient =
        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build()

    protected fun createRequest(uri: URI): HttpRequest =
        HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5)).GET().build()

    abstract suspend fun proxy(ip: String): Reason?
}