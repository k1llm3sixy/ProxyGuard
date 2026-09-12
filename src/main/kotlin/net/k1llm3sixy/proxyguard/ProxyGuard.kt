package net.k1llm3sixy.proxyguard

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.k1llm3sixy.proxyguard.command.ProxyGuardCommand
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.listener.PreLoginListener
import net.k1llm3sixy.proxyguard.services.DbService
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(
    id = "proxyguard",
    name = "ProxyGuard",
    version = "0.1.0",
    description = "Plugin to detect and block PROXY, VPN connections",
    authors = ["n3vvx", "k1llm3sixy"],
    dependencies = [Dependency(id = "mckotlin-velocity")]
)
class ProxyGuard @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDir: Path,
    val metrics: Metrics.Factory,
)
{
    companion object
    {
        lateinit var scope: CoroutineScope
        lateinit var LOGGER: Logger
    }

    @Subscribe
    fun onProxyInitialization(e: ProxyInitializeEvent)
    {
        val id = 33992
        metrics.make(this, id)
        LOGGER = logger
        scope = CoroutineScope(Dispatchers.IO)
        Storage.init(dataDir)
        DbService.init()

        registerCmd()

        server.eventManager.register(
            this,
            PreLoginListener()
        )
    }

    private fun registerCmd()
    {
        val manager = server.commandManager
        val meta = manager.metaBuilder("proxyguard").aliases("pg").plugin(this).build()

        manager.register(
            meta,
            ProxyGuardCommand.create()
        )
    }
}
