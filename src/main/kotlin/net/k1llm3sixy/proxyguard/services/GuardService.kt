package net.k1llm3sixy.proxyguard.services

import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.provider.BaseProvider
import net.k1llm3sixy.proxyguard.provider.Detection
import java.util.*

object GuardService
{
    suspend fun detect(ip: String, uuid: UUID, nick: String): Detection
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
            return Detection.CLEAN
        }

        val userDetection = DbService.getUserDetection(uuid)

        if (userDetection != Detection.CLEAN)
        {
            LOGGER.debug(
                "Rejected {} ({}) {} - already in bad_users",
                nick,
                ip,
                userDetection
            )
            return userDetection
        }
        val detection = BaseProvider.provider().classify(ip)

        if (detection != Detection.CLEAN)
        {
            DbService.addUser(
                uuid,
                nick,
                ip,
                detection
            )
            DiscordService.sendWebhook(
                nick,
                ip,
                detection.name
            )

            LOGGER.info(
                "Blocked {} ({}) - {}",
                nick,
                ip,
                detection
            )
            return detection
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
        return Detection.CLEAN
    }

    suspend fun checkIp(ip: String): Detection
    {
        val detection = BaseProvider.provider().classify(ip)

        return detection
    }
}