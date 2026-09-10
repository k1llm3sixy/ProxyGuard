package net.k1llm3sixy.proxyguard.provider

import com.google.gson.JsonParser
import java.net.URI

class IpApiProvider : BaseProvider()
{
    override val provider = Provider.IP_API

    override suspend fun proxy(ip: String): Boolean
    {
        val uri = provider.url.format(ip)
        return getResult(URI.create(uri)) {
            val json = JsonParser.parseString(it.body()).asJsonObject
            val proxy = json.get("proxy")?.asBoolean ?: false
            val hosting = json.get("hosting")?.asBoolean ?: false

            proxy || hosting
        }
    }

}