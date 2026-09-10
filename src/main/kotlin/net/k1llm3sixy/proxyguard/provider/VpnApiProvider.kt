package net.k1llm3sixy.proxyguard.provider

import com.google.gson.JsonParser
import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import java.net.URI

class VpnApiProvider : BaseProvider()
{
    override val provider = Provider.VPN_API

    override suspend fun proxy(ip: String): Boolean
    {
        val key = CONFIG.getString(Route.from("vpn-api-key"))
        val uri = provider.url.format(
            ip,
            key
        )

        return getResult(URI.create(uri)) {
            val json = JsonParser.parseString(it.body()).asJsonObject
            val security = json.get("security").asJsonObject ?: return@getResult false

            val vpn = security.get("vpn")?.asBoolean ?: false
            val proxy = security.get("proxy")?.asBoolean ?: false

            vpn || proxy
        }
    }
}
