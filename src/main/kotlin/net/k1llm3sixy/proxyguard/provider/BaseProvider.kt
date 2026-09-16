package net.k1llm3sixy.proxyguard.provider

import com.google.gson.Gson
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import net.k1llm3sixy.proxyguard.io.Check
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class Reason
{
    VPN,
    PROXY,
    EMPTY,
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
            else  -> Reason.EMPTY
        }

        Check.VPN   -> if (vpn) Reason.VPN else Reason.EMPTY
        Check.PROXY -> if (proxy) Reason.PROXY else Reason.EMPTY
    }

    protected suspend fun getResult(uri: String, block: (HttpResponse<String>) -> Reason): Reason
    {
        return withTimeout(5.seconds) {
            runCatching {
                val response = client.sendAsync(
                    createRequest(URI.create(uri)),
                    HttpResponse.BodyHandlers.ofString()
                ).await()

                if (response.statusCode() == 200)
                {
                    block(response)
                }
                else Reason.EMPTY
            }.getOrDefault(Reason.EMPTY)
        }
    }

    protected val gson = Gson()

    protected val client: HttpClient =
        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5)).build()

    protected fun createRequest(uri: URI): HttpRequest =
        HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5)).GET().build()

    abstract suspend fun proxy(ip: String): Reason
}