package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.ext.text
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object UsersCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("users").executes {
            scope.launch {
                val users = DbService.getUsers()

                if (users.isEmpty())
                {
                    it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_USER_EMPTY))
                    return@launch
                }

                users.forEach { (uuid, nick, ip) ->
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
                            .build()
                    )

                    it.source.sendMessage(msg)
                }
            }

            Command.SINGLE_SUCCESS
        }
}