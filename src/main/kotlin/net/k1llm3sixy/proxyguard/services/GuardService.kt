package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import java.util.*

object GuardService
{
    suspend fun block(ip: String, uuid: UUID, nick: String): Boolean
    {
        val local = ip == "127.0.0.1" || ip == "localhost"
        val bypass = DbService.getWhitelist(ip) || DbService.getValidUser(uuid)
        if (local || bypass)
        {
            LOGGER.debug(
                "Skipped check for {} ({}) - IP is local connection or bypass",
                nick,
                ip
            )
            return false
        }

        if (DbService.getUser(uuid))
        {
            LOGGER.debug(
                "Rejected {} ({}) - already in bad_users",
                nick,
                ip
            )
            return true
        }
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

            LOGGER.info(
                "Blocked {} ({}) - proxy/VPN detected",
                nick,
                ip
            )
            return true
        }

        DbService.addValidUser(
            uuid,
            nick,
            ip
        )

        LOGGER.debug(
            "Allowed {} ({}) - provider check passed, adding to cache",
            nick,
            ip
        )
        return false
    }
}