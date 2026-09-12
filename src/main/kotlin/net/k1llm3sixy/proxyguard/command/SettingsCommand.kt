package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource

object SettingsCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("settings")
            .then(ProviderCommand.create())
            .then(DiscordCommand.create())
}