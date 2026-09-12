package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object UsersCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("users").executes {
            scope.launch {
                val users = DbService.getUsers()

                if (users.isEmpty())
                {
                    it.source.sendRichMessage(CONFIG.getString(Route.from("msg-users-empty")))
                    return@launch
                }

                for ((uuid, nick, ip) in users)
                {
                    val msg = miniMsg.deserialize(
                        CONFIG.getString(Route.from("msg-users")),
                        Placeholder.component(
                            "nick",
                            Component.text(nick)
                        ),
                        Placeholder.component(
                            "ip",
                            Component.text(ip)
                        ),
                        Placeholder.component(
                            "uuid",
                            Component.text(uuid.toString())
                        )
                    )

                    it.source.sendMessage(msg)
                }
            }

            Command.SINGLE_SUCCESS
        }
}