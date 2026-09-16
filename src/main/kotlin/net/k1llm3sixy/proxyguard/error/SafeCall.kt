package net.k1llm3sixy.proxyguard.error

import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import kotlin.coroutines.cancellation.CancellationException

enum class GuardError(val message: String)
{
    CONFIG_INIT("Failed to initialize config"),
    CONFIG_RELOAD("Failed to reload config"),
    DB_INIT("Failed to initialize and connect database"),
    DB_QUERY("Failed to execute db query"),
    DS_SEND_WEBHOOK("Failed to send discord webhook"),
    LOGIN_EVENT("Error occurred while processing player login"),
    B_STATS("Failed to send bstats metrics"),
}

inline fun <T> safeCall(
    error: GuardError,
    block: () -> T,
): Result<T>
{
    return try
    {
        Result.success(block())
    } catch (e: Exception)
    {
        if (e is CancellationException) throw e

        LOGGER.error(
            "${error.message}: ${e.message}",
            e
        )

        Result.failure(
            Exception(
                error.message,
                e
            )
        )
    }
}