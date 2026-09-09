package net.k1llm3sixy.proxyguard.io

import dev.dejvokep.boostedyaml.YamlDocument
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings
import net.k1llm3sixy.proxyguard.ProxyGuard
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import java.io.File
import java.nio.file.Path

object Config
{
    lateinit var CONFIG: YamlDocument
        private set

    fun init(dataDir: Path)
    {
        try
        {
            CONFIG = createConfig(dataDir)
            CONFIG.update()
            CONFIG.save()
        } catch (e: Exception)
        {
            // TODO: ошибка создания конфига
            LOGGER.error(
                "Не удалось создать конфиг",
                e
            )
        }
    }

    private fun createConfig(dataDir: Path) = YamlDocument.create(
        File(
            dataDir.toFile(),
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