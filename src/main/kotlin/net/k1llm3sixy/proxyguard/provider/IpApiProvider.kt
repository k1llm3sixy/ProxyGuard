package net.k1llm3sixy.proxyguard.provider

import com.google.gson.JsonParser
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class IpApiProvider : BaseProvider()
{
    override val provider = Provider.IP_API

    override suspend fun proxy(ip: String): Boolean
    {
        val uri = provider.url.format(ip)
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
                    val proxy = json.get("proxy")?.asBoolean ?: false
                    val hosting = json.get("hosting")?.asBoolean ?: false

                    println("JSON - $json")

                    proxy || hosting
                }
                else
                {
                    false
                }
            }.getOrDefault(false)
        } ?: false
    }

}