package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import net.k1llm3sixy.proxyguard.provider.Reason
import java.util.*

object GuardService
{
    suspend fun block(ip: String, uuid: UUID, nick: String): Reason?
    {
        val local = ip == "127.0.0.1" || ip == "localhost"
        val bypass = DbService.getWhitelist(ip) || DbService.getValidUser(uuid)
        if (local || bypass)
        {
            LOGGER.debug(
                "Skipped check for {} ({}) - IP is local or bypass",
                nick,
                ip
            )
            return null
        }

        if (DbService.getUser(uuid))
        {
            val reason = DbService.getUserReason(uuid)
            LOGGER.debug(
                "Rejected {} ({}) {} - already in bad_users",
                nick,
                ip,
                reason
            )
            return reason
        }
        val reason = BaseProvider.provider().proxy(ip)

        if (reason != null)
        {
            DbService.addUser(
                uuid,
                nick,
                ip,
                reason
            )
            DiscordService.sendWebhook(
                nick,
                ip,
                reason.name
            )

            LOGGER.info(
                "Blocked {} ({}) - {}",
                nick,
                ip,
                reason
            )
            return reason
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
        return null
    }
}