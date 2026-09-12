package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import dev.dejvokep.boostedyaml.route.Route
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG

object ReloadCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("reload").executes {
            CONFIG.reload()
            it.source.sendRichMessage(CONFIG.getString(Route.from("msg-config-reload")))

            Command.SINGLE_SUCCESS
        }
}