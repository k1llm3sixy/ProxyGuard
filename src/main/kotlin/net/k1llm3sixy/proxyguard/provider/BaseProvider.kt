package net.k1llm3sixy.proxyguard.provider

import com.google.gson.Gson
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    protected val gson = Gson()

    protected val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    protected fun createRequest(uri: URI): HttpRequest =
        HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(5)).GET().build()

    protected suspend fun getResult(uri: URI, block: (HttpResponse<String>) -> Boolean): Boolean
    {
        return withTimeoutOrNull(5.seconds) {
            runCatching {
                val response = client.sendAsync(
                    createRequest(uri),
                    HttpResponse.BodyHandlers.ofString()
                ).await()

                response.statusCode() == 200 && block(response)
            }.getOrDefault(false)
        } ?: false
    }

    abstract suspend fun proxy(ip: String): Boolean

    abstract val provider: Provider
    abstract val key: String
}