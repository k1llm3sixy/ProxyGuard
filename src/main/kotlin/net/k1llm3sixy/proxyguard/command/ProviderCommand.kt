package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.provider.Provider

object ProviderCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("provider").then(
            BrigadierCommand.requiredArgumentBuilder(
                "provider",
                StringArgumentType.word()
            ).suggests { _, builder ->
                Provider.entries.forEach {
                    builder.suggest(it.name)
                }
                builder.buildFuture()
            }.executes {
                val provider = StringArgumentType.getString(
                    it,
                    "provider"
                )
                Storage.setProvider(provider)
                it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_SET_PROVIDER))

                Command.SINGLE_SUCCESS
            }
        )
}