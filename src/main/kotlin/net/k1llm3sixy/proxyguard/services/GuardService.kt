package net.k1llm3sixy.proxyguard.services

import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import java.util.*

object GuardService
{
    val kickMessage: String
        get() = CONFIG.getString(Route.from("kick-message"))

    suspend fun block(ip: String, uuid: UUID, nick: String): Boolean
    {
        if (ip == "127.0.0.1" || ip == "localhost" || DbService.getWhitelist(ip)) return false

        if (DbService.getUser(uuid)) return true
        val proxy = BaseProvider.provider().proxy(ip)

        if (proxy)
        {
            DbService.addUser(
                uuid,
                nick,
                ip
            )
            DiscordService.sendWebhook(
                nick,
                ip
            )
            return true
        }

        return false
    }
}