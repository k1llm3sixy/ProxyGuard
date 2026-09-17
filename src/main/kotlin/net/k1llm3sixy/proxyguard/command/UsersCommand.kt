package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.ext.hasPerms
import net.k1llm3sixy.proxyguard.ext.text
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object UsersCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    override fun create() =
        BrigadierCommand.literalArgumentBuilder("users")
            .requires { it.hasPerms("proxyguard.users") }
            .executes {
                scope.launch {
                    val users = DbService.getUsers()

                    if (users.isEmpty())
                    {
                        it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_USER_EMPTY))
                        return@launch
                    }

                    val title = miniMsg.deserialize(
                        ConfigRoute.MSG_USERS_TITLE,
                        Placeholder.component(
                            "count",
                            Component.text(users.size)
                        )
                    )

                    it.source.sendMessage(title)

                    users.forEach { (uuid, nick, ip, detection, time) ->
                        val msg = miniMsg.deserialize(
                            ConfigRoute.MSG_USERS,
                            TagResolver.builder()
                                .text(
                                    "nick",
                                    nick
                                )
                                .text(
                                    "ip",
                                    ip
                                )
                                .text(
                                    "uuid",
                                    uuid
                                )
                                .text(
                                    "detection",
                                    detection
                                )
                                .text(
                                    "time",
                                    time
                                )
                                .build()
                        )

                        it.source.sendMessage(msg)
                    }
                }

                Command.SINGLE_SUCCESS
            }
}