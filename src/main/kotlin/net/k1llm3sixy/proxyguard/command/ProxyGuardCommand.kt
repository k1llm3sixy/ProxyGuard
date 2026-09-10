package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.provider.Provider
import net.k1llm3sixy.proxyguard.services.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object ProxyGuardCommand
{
    private val providerCmd =
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

                Command.SINGLE_SUCCESS
            }
        )

    private val usersCmd = BrigadierCommand.literalArgumentBuilder("users").executes {
        scope.launch {
            val users = Database.getUsers()

            if (users.isEmpty())
            {
                it.source.sendRichMessage(CONFIG.getString(Route.from("msg-users-empty")))
                return@launch
            }

            for ((uuid, nick, ip) in users)
            {
                val msg = MiniMessage.miniMessage().deserialize(
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

    private val reloadCmd = BrigadierCommand.literalArgumentBuilder("reload").executes {
        CONFIG.reload()
        it.source.sendRichMessage(CONFIG.getString(Route.from("msg-config-reload")))

        Command.SINGLE_SUCCESS
    }

    private val unbanCmd = BrigadierCommand.literalArgumentBuilder("unban")
        .then(
            BrigadierCommand.requiredArgumentBuilder(
                "uuid",
                StringArgumentType.greedyString()
            ).suggests { _, builder ->
                val users = Database.getUsers()

                for ((uuid) in users)
                {
                    builder.suggest(uuid.toString())
                }

                builder.buildFuture()
            }.executes {
                // TODO: добавить, что uuid нет такого
                scope.launch {
                    val uuid = StringArgumentType.getString(
                        it,
                        "uuid"
                    )
                    val result = Database.removeUser(uuid)

                    if (result)
                    {
                        val msg = MiniMessage.miniMessage().deserialize(
                            CONFIG.getString(Route.from("msg-unban-user")),
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

    fun create(): BrigadierCommand
    {
        val node =
            BrigadierCommand.literalArgumentBuilder("proxyguard").requires { it.hasPermission("proxyguard.admin") }
                .then(reloadCmd)
                .then(providerCmd)
                .then(usersCmd)
                .then(unbanCmd)
                .build()

        return BrigadierCommand(node)
    }
}