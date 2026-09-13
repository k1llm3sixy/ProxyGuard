package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object DiscordCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource> =
        BrigadierCommand.literalArgumentBuilder("discord")
            .then(
                BrigadierCommand.literalArgumentBuilder("enable")
                    .executes {
                        Storage.toggleDs(true)

                        it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_DS_ENABLE))
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                BrigadierCommand.literalArgumentBuilder("disable")
                    .executes {
                        Storage.toggleDs(false)

                        it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_DS_DISABLE))
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
                            Storage.setDsWebhook(
                                ConfigRoute.DS_WEBHOOK,
                                url
                            )

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
                                    Storage.setDsWebhook(
                                        ConfigRoute.DS_EMBED_TITLE,
                                        text
                                    )

                                    val msg = miniMsg.deserialize(
                                        CONFIG.get(ConfigRoute.MSG_DS_EMBED_TITLE),
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
                                    Storage.setDsWebhook(
                                        ConfigRoute.DS_EMBED_DESC,
                                        text
                                    )

                                    val msg = miniMsg.deserialize(
                                        CONFIG.get(ConfigRoute.MSG_DS_EMBED_DESC),
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
                                    Storage.setDsWebhook(
                                        ConfigRoute.DS_EMBED_REASON,
                                        text
                                    )

                                    val msg = miniMsg.deserialize(
                                        CONFIG.get(ConfigRoute.MSG_DS_EMBED_REASON),
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
}