package net.k1llm3sixy.proxyguard.listener

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.provider.Detection
import net.k1llm3sixy.proxyguard.services.GuardService
import net.kyori.adventure.text.Component

class PreLoginListener
{
    @Subscribe
    fun onPreLogin(event: PreLoginEvent, continuation: Continuation)
    {
        scope.launch {
            safeCall(GuardError.LOGIN_EVENT) {
                val ip = event.connection.remoteAddress.address.hostAddress
                val uuid = event.uniqueId!!
                val nick = event.username

                val detection = GuardService.detect(
                    ip,
                    uuid,
                    nick
                )

                if (detection != Detection.CLEAN)
                {
                    event.result = PreLoginEvent.PreLoginComponentResult.denied(
                        Component.text(Storage.getKickMsg(detection))
                    )
                }

                continuation.resume()
            }.onFailure {
                continuation.resumeWithException(it)
            }
        }
    }
}