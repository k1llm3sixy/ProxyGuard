package net.k1llm3sixy.proxyguard.listener

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import net.k1llm3sixy.proxyguard.services.Database
import net.kyori.adventure.text.Component

class PreLoginListener
{
    private val kickMessage = CONFIG.getString(Route.from("kick-message"))

    @Subscribe
    fun onPreLogin(event: PreLoginEvent, continuation: Continuation)
    {
        scope.launch {
            try
            {
                // TODO: отрефакторить все это говно
                val ip = event.connection.remoteAddress.address.hostAddress
                val uuid = event.uniqueId!!

                if (ip == "127.0.0.1")
                {
                    continuation.resume()
                    return@launch
                }

                if (Database.getUser(uuid))
                {
                    event.result = PreLoginEvent.PreLoginComponentResult.denied(Component.text(kickMessage))
                    continuation.resume()
                    return@launch
                }

                Database.insertUser(
                    uuid,
                    event.username,
                    ip
                )

                val proxy = BaseProvider.provider().proxy(ip)

                if (proxy)
                {
                    event.result =
                        PreLoginEvent.PreLoginComponentResult.denied(Component.text(kickMessage))
                }

                continuation.resume()
            } catch (e: Throwable)
            {
                continuation.resumeWithException(e)
            }
        }
    }
}