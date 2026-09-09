package net.k1llm3sixy.proxyguard.listener

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import net.kyori.adventure.text.Component

class PreLoginListener
{
    @Subscribe
    fun onPreLogin(event: PreLoginEvent, continuation: Continuation)
    {
        scope.launch {
            try
            {
                // TODO: после тестов вернуть нормальный ip
                //val ip = event.connection.remoteAddress.address.hostAddress
                val ip = "0.0.0.0"

                if (ip == "127.0.0.1") return@launch

                val isProxy = BaseProvider.getProvider().proxy(ip)

                if (isProxy)
                {
                    event.result = PreLoginEvent.PreLoginComponentResult.denied(Component.text("ЛОХ"))
                }

                continuation.resume()
            } catch (e: Throwable)
            {
                continuation.resumeWithException(e)
            }
        }
    }
}