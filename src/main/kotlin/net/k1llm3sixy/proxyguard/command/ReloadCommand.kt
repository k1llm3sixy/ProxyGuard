package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG

object ReloadCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    override fun create() =
        BrigadierCommand.literalArgumentBuilder("reload").executes {
            Storage.reload()
            it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_CONFIG_RELOAD))

            Command.SINGLE_SUCCESS
        }
}