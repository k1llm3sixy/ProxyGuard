package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.ext.hasPerms
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.services.DbService
import net.k1llm3sixy.proxyguard.services.Type
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object UnbanCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    private fun unban(type: Type) = BrigadierCommand.requiredArgumentBuilder(
        "arg",
        greedyString()
    ).suggests { _, builder ->
        val data = DbService.getUsersData(type)
        data.forEach { builder.suggest(it) }
        builder.buildFuture()
    }.executes {
        val arg = StringArgumentType.getString(
            it,
            "arg"
        )

        val result = DbService.removeUser(
            type,
            arg
        )

        if (result)
        {
            val msg = miniMsg.deserialize(
                ConfigRoute.MSG_UNBAN,
                Placeholder.component(
                    "type",
                    Component.text(arg)
                )
            )

            it.source.sendMessage(msg)
        }

        Command.SINGLE_SUCCESS
    }

    override fun create() =
        BrigadierCommand.literalArgumentBuilder("unban").requires { it.hasPerms("proxyguard.unban") }
            .then(BrigadierCommand.literalArgumentBuilder("nick").then(unban(Type.NICK)))
            .then(BrigadierCommand.literalArgumentBuilder("uuid").then(unban(Type.UUID)))
            .then(BrigadierCommand.literalArgumentBuilder("ip").then(unban(Type.IP)))
}