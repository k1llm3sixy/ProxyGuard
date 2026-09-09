package net.k1llm3sixy.proxyGuard;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger

@Plugin(
    id = "proxyguard",
    dependencies = [
        Dependency(id = "mckotlin-velocity")
    ]
)
class ProxyGuard @Inject constructor(val server: ProxyServer, val logger: Logger)
{
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent)
    {
    }
}
