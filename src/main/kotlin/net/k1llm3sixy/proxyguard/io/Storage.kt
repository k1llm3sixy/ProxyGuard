package net.k1llm3sixy.proxyguard.io

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.route.Route
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.k1llm3sixy.proxyguard.ProxyGuard
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import java.io.File
import java.nio.file.Path

// TODO: сделать сейф коллы для большинства методов
object Storage
{
    lateinit var CONFIG: YamlDocument
        private set

    private lateinit var dataFolder: Path

    fun init(dataDir: Path)
    {
        try
        {
            dataFolder = dataDir
            CONFIG = createConfig()
            CONFIG.update()
            CONFIG.save()

            createDb()
        } catch (e: Exception)
        {
            LOGGER.error(
                "Failed to create config: ${e.message}",
                e
            )
        }
    }

    fun setProvider(provider: String)
    {
        CONFIG.set(
            Route.from("provider"),
            provider
        )
        CONFIG.save()
        CONFIG.reload()
    }

    fun getDbPath(): String = File(
        dataFolder.toFile(),
        "proxyguard.db"
    ).absolutePath

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