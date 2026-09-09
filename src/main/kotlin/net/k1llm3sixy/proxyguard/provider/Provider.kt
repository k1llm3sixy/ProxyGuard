package net.k1llm3sixy.proxyguard.provider

enum class Provider(val url: String)
{
    IP_API("http://ip-api.com/json/%s?fields=proxy,hosting"),

    VPN_API("https://vpnapi.io/api/%s?key=%s"),

    // TODO: апиху прикрутить
    PROXY_CHECK("https://proxycheck.io/");
}