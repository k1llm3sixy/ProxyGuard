package net.k1llm3sixy.proxyguard.provider

import java.net.URI

class IpApiProvider : BaseProvider()
{
    private data class Response(
        val proxy: Boolean,
        val hosting: Boolean,
    )

    override val provider = Provider.IP_API

    override suspend fun proxy(ip: String): Boolean
    {
        val uri = provider.url.format(ip)
        return getResult(URI.create(uri)) {
            val data = gson.fromJson(
                it.body(),
                Response::class.java
            ) ?: return@getResult false

            data.proxy || data.hosting
        }
    }

}