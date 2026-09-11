package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.velocitypowered.api.command.BrigadierCommand
import dev.dejvokep.boostedyaml.route.Route
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.provider.Provider
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object ProxyGuardCommand
{
    private val miniMsg = MiniMessage.miniMessage()

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

    private val discordCmd = BrigadierCommand.literalArgumentBuilder("discord")
        .then(
            BrigadierCommand.literalArgumentBuilder("enable")
                .executes {
                    Storage.enableDs()

                    it.source.sendRichMessage(CONFIG.getString(Route.from("msg-ds-enable")))
                    Command.SINGLE_SUCCESS
                }
        )
        .then(
            BrigadierCommand.literalArgumentBuilder("disable")
                .executes {
                    Storage.disableDs()

                    it.source.sendRichMessage(CONFIG.getString(Route.from("msg-ds-disable")))
                    Command.SINGLE_SUCCESS
                }
        )
        .then(
            BrigadierCommand.literalArgumentBuilder("webhook")
                .then(
                    BrigadierCommand.requiredArgumentBuilder(
                        "url",
                        greedyString()
                    ).executes {
                        val url = StringArgumentType.getString(
                            it,
                            "url"
                        )
                        Storage.setDsWebhook(url)

                        Command.SINGLE_SUCCESS
                    }
                )
        )
        .then(
            BrigadierCommand.literalArgumentBuilder("embed")
                .then(
                    BrigadierCommand.literalArgumentBuilder("title")
                        .then(
                            BrigadierCommand.requiredArgumentBuilder(
                                "text",
                                greedyString()
                            ).executes {
                                val text = StringArgumentType.getString(
                                    it,
                                    "text"
                                )
                                Storage.setEmbedTitle(text)

                                val msg = miniMsg.deserialize(
                                    CONFIG.getString("msg-ds-embed-title"),
                                    Placeholder.component(
                                        "title",
                                        Component.text(text)
                                    )
                                )
                                it.source.sendMessage(msg)
                                Command.SINGLE_SUCCESS
                            }
                        )
                )
                .then(
                    BrigadierCommand.literalArgumentBuilder("description")
                        .then(
                            BrigadierCommand.requiredArgumentBuilder(
                                "text",
                                greedyString()
                            ).executes {
                                val text = StringArgumentType.getString(
                                    it,
                                    "text"
                                )
                                Storage.setEmbedDescription(text)

                                val msg = miniMsg.deserialize(
                                    CONFIG.getString("msg-ds-embed-description"),
                                    Placeholder.component(
                                        "description",
                                        Component.text(text)
                                    )
                                )
                                it.source.sendMessage(msg)
                                Command.SINGLE_SUCCESS
                            }
                        )
                )
                .then(
                    BrigadierCommand.literalArgumentBuilder("reason")
                        .then(
                            BrigadierCommand.requiredArgumentBuilder(
                                "text",
                                greedyString()
                            ).executes {
                                val text = StringArgumentType.getString(
                                    it,
                                    "text"
                                )
                                Storage.setEmbedReason(text)

                                val msg = miniMsg.deserialize(
                                    CONFIG.getString("msg-ds-embed-reason"),
                                    Placeholder.component(
                                        "reason",
                                        Component.text(text)
                                    )
                                )
                                it.source.sendMessage(msg)
                                Command.SINGLE_SUCCESS
                            }
                        )
                )
        )

    private val settingsCmd = BrigadierCommand.literalArgumentBuilder("settings")
        .then(providerCmd)
        .then(discordCmd)


    private val usersCmd = BrigadierCommand.literalArgumentBuilder("users").executes {
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

    private val reloadCmd = BrigadierCommand.literalArgumentBuilder("reload").executes {
        CONFIG.reload()
        it.source.sendRichMessage(CONFIG.getString(Route.from("msg-config-reload")))

        Command.SINGLE_SUCCESS
    }

    private val unbanCmd = BrigadierCommand.literalArgumentBuilder("unban")
        .then(
            BrigadierCommand.requiredArgumentBuilder(
                "uuid",
                greedyString()
            ).suggests { _, builder ->
                val users = DbService.getUsers()

                for ((uuid) in users)
                {
                    builder.suggest(uuid.toString())
                }

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
                .then(settingsCmd)
                .then(usersCmd)
                .then(unbanCmd)
                .build()

        return BrigadierCommand(node)
    }
}