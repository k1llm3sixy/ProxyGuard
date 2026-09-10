package net.k1llm3sixy.proxyguard.provider

enum class Provider(val url: String)
{
    IP_API("http://ip-api.com/json/%s?fields=proxy,hosting"),
    VPN_API("https://vpnapi.io/api/%s?key=%s"),
    PROXY_CHECK("http://proxycheck.io/v3/%s?key=%s&short=1");
}