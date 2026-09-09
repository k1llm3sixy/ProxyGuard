package net.k1llm3sixy.proxyguard.provider

class ProxyCheckProvider : BaseProvider()
{
    override val provider = Provider.PROXY_CHECK
    override suspend fun proxy(ip: String): Boolean
    {
        // TODO: доделать провайдер
        TODO("Not yet implemented")
    }
}
