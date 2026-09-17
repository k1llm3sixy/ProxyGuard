package net.k1llm3sixy.proxyguard.services

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.ext.getB
import net.k1llm3sixy.proxyguard.io.ConfigRoute
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
    @SerializedName("icon_url")
    val icon: String,
)

object DiscordService
{
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    fun sendWebhook(nick: String, ip: String, detection: String)
    {
        if (!CONFIG.getB(ConfigRoute.DS_ENABLED)) return

        val playerField = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_PLAYER)
        val detectionField = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_DETECTION)
        val ipField = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_IP)

        val playerV = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_PLAYER_VALUE)
        val detectionV = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_DETECTION_VALUE)
        val ipV = CONFIG.get(ConfigRoute.DS_EMBED_FIELD_IP_VALUE)

        val title = CONFIG.get(ConfigRoute.DS_EMBED_TITLE)
        val description = CONFIG.get(ConfigRoute.DS_EMBED_DESC)
        val icon = "https://i.ibb.co/Hfv2KYRR/icon.png"

        val uri = URI.create(CONFIG.get(ConfigRoute.DS_WEBHOOK))
        val payload = WebhookPayload(
            listOf(
                Embed(
                    title,
                    description,
                    15158332,
                    listOf(
                        Field(
                            playerField,
                            playerV.replace(
                                "{nick}",
                                nick
                            ),
                            true
                        ),
                        Field(
                            detectionField,
                            detectionV.replace(
                                "{detection}",
                                detection
                            ),
                            true
                        ),
                        Field(
                            ipField,
                            ipV.replace(
                                "{ip}",
                                ip
                            ),
                            false
                        )
                    ),
                    Footer(
                        "Proxy Guard",
                        icon
                    ),
                    "${Instant.now()}"
                )
            )
        )

        safeCall(GuardError.DS_SEND_WEBHOOK) {
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(payload)))
                .build()

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
