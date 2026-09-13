package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object UnbanCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("unban")
            .then(
                BrigadierCommand.requiredArgumentBuilder(
                    "uuid",
                    greedyString()
                ).suggests { _, builder ->
                    val uuids = DbService.getUsersUuid()
                    uuids.forEach { builder.suggest(it) }

                    builder.buildFuture()
                }.executes {
                    scope.launch {
                        val uuid = StringArgumentType.getString(
                            it,
                            "uuid"
                        )
                        val result = DbService.removeUser(uuid)

                        if (result)
                        {
                            val msg = miniMsg.deserialize(
                                ConfigRoute.MSG_UNBAN_USER,
                                Placeholder.component(
                                    "uuid",
                                    Component.text(uuid)
                                )
                            )
                            it.source.sendMessage(msg)
                        }
                    }

                    Command.SINGLE_SUCCESS
                }
            )
}