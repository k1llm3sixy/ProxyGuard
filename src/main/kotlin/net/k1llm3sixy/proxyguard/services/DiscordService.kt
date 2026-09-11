package net.k1llm3sixy.proxyguard.services

import com.google.gson.Gson
import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant

private data class WebhookPayload(
    val embeds: List<Embed>,
)

private data class Embed(
    val title: String,
    val description: String,
    val color: Int,
    val fields: List<Field>,
    val footer: Footer,
    val timestamp: String,
)

private data class Field(
    val name: String,
    val value: String,
    val inline: Boolean,
)

private data class Footer(
    val text: String,
)

object DiscordService
{
    private val client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
    private val ds = Route.from("discord")

    fun sendWebhook(nick: String, ip: String)
    {
        if (!CONFIG.getBoolean(ds.add("enabled"))) return

        val title = CONFIG.getString(ds.add("embed-title"))
        val description = CONFIG.getString(ds.add("embed-description"))
        val reason = CONFIG.getString(ds.add("embed-reason"))

        val uri = URI.create(CONFIG.getString(ds.add("webhook")))
        val payload = WebhookPayload(
            listOf(
                Embed(
                    title,
                    description,
                    15158332,
                    listOf(
                        Field(
                            "Player",
                            "`$nick`",
                            true
                        ),
                        Field(
                            "Reason",
                            reason,
                            true
                        ),
                        Field(
                            "IP",
                            "||`$ip`||",
                            false
                        )
                    ),
                    Footer(
                        "Proxy Guard"
                    ),
                    "${Instant.now()}"
                )
            )
        )

        safeCall(GuardError.DS_SEND_WEBHOOK) {
            val request = HttpRequest.newBuilder().uri(uri).setHeader(
                "Content-Type",
                "application/json"
            ).POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(payload))).build()

            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )

            val code = response.statusCode()

            if (code != 204 && code != 200)
            {
                LOGGER.error(
                    "Bad discord response: $code"
                )
            }
        }
    }
}
