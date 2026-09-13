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
        val local = ip == "127.0.0.1" || ip == "localhost"
        val bypass = DbService.getWhitelist(ip) || DbService.getValidUser(uuid)
        if (local || bypass) return false

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

        DbService.addValidUser(uuid, nick, ip)

        return false
    }
}