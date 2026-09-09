package net.k1llm3sixy.proxyguard.provider

import com.google.gson.JsonParser
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import net.k1llm3sixy.proxyguard.io.Config.CONFIG
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class VpnApiProvider : BaseProvider()
{
    override val provider = Provider.VPN_API

    override suspend fun proxy(ip: String): Boolean
    {
        val key = CONFIG.getString(Route.from("vpn-api-key"))
        val uri = provider.url.format(ip, key)
        val request = HttpRequest.newBuilder().uri(URI.create(uri)).timeout(Duration.ofSeconds(5)).GET().build()

        return withTimeoutOrNull(5.seconds) {
            runCatching {
                val response = client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                ).await()

                if (response.statusCode() == 200)
                {
                    val json = JsonParser.parseString(response.body()).asJsonObject
                    val security = json.get("security").asJsonObject ?: return@runCatching false

                    val vpn = security.get("vpn")?.asBoolean ?: false
                    val proxy = security.get("proxy")?.asBoolean ?: false

                    println("JSON - $json")

                    vpn || proxy
                }
                else
                {
                    false
                }
            }.getOrDefault(false)
        } ?: false
    }
}
