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
import java.io.File
import java.nio.file.Path

object Storage
{
    lateinit var CONFIG: YamlDocument
        private set

    private lateinit var dataFolder: Path

    private val dsRoute = Route.from("discord")

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

    fun setProvider(provider: String)
    {
        CONFIG.set(
            Route.from("provider"),
            provider
        )
        save()
    }

    fun toggleDs(state: Boolean)
    {
        CONFIG.set(
            dsRoute.add("enabled"),
            state
        )
        save()
    }

    fun setDsWebhook(route: String, text: String)
    {
        CONFIG.set(
            dsRoute.add(route),
            text
        )
        save()
    }

    fun getDbPath(): String = safeCall(GuardError.DB_GET) {
        File(
            dataFolder.toFile(),
            "proxyguard.db"
        ).absolutePath
    }.getOrThrow()

    private fun save()
    {
        CONFIG.save()
        CONFIG.reload()
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