package net.k1llm3sixy.proxyguard.io

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.route.Route
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.k1llm3sixy.proxyguard.ProxyGuard
import net.k1llm3sixy.proxyguard.error.GuardError
import net.k1llm3sixy.proxyguard.error.safeCall
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.provider.Reason

import java.io.File
import java.nio.file.Path

enum class ConfigRoute(val route: Route)
{
    VPN_API_KEY(Route.from("vpn-api-key")),
    PROXYCHECK_API_KEY(Route.from("proxycheck-api-key")),

    MSG_CONFIG_RELOAD(Route.from("msg-config-reload")),
    MSG_USER_EMPTY(Route.from("msg-users-empty")),
    MSG_USERS_TITLE(Route.from("msg-users-title")),
    MSG_USERS(Route.from("msg-users")),
    MSG_UNBAN(Route.from("msg-unban")),

    KICK_PROXY_MSG(Route.from("kick-proxy-msg")),
    KICK_VPN_MSG(Route.from("kick-vpn-msg")),

    MSG_INVALID_IP(Route.from("msg-invalid-ip")),

    MSG_WHITELIST_ADD(Route.from("msg-whitelist-add")),
    MSG_WHITELIST_REMOVE(Route.from("msg-whitelist-remove")),
    MSG_WHITELIST_LIST(Route.from("msg-whitelist-list")),
    MSG_WHITELIST_EMPTY(Route.from("msg-whitelist-empty")),

    DS_ENABLED(Route.from("discord").add("enabled")),
    DS_WEBHOOK(Route.from("discord").add("webhook")),
    DS_EMBED_TITLE(Route.from("discord").add("embed-title")),
    DS_EMBED_DESC(Route.from("discord").add("embed-description")),
}

enum class Check
{
    ALL,
    PROXY,
    VPN
}

object Storage
{
    lateinit var CONFIG: YamlDocument
        private set

    private lateinit var dataFolder: Path

    fun init(dataDir: Path)
    {
        safeCall(GuardError.CONFIG_INIT) {
            dataFolder = dataDir
            CONFIG = createConfig()
            CONFIG.update()
            CONFIG.save()

            createDb()
        }.getOrThrow()
    }

    fun getCheck(): Check = CONFIG.getEnum(
        Route.from("check"),
        Check::class.java
    )

    fun getKickMsg(reason: Reason): String = when (reason)
    {
        Reason.PROXY -> CONFIG.get(ConfigRoute.KICK_PROXY_MSG)
        Reason.VPN   -> CONFIG.get(ConfigRoute.KICK_VPN_MSG)
    }

    fun getDbPath(): String = safeCall(GuardError.DB_GET) {
        File(
            dataFolder.toFile(),
            "proxyguard.db"
        ).absolutePath
    }.getOrThrow()

    fun reload()
    {
        safeCall(GuardError.CONFIG_RELOAD) {
            CONFIG.reload()
        }
    }

    private fun createDb()
    {
        val file = File(
            dataFolder.toFile(),
            "proxyguard.db"
        )
        if (!file.exists()) file.createNewFile()
    }

    private fun createConfig() = YamlDocument.create(
        File(
            dataFolder.toFile(),
            "config.yml"
        ),
        ProxyGuard::class.java.getResourceAsStream("/config.yml")!!,
        GeneralSettings.DEFAULT,
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(BasicVersioning("file-version"))
            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
    )
}