package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.LOGGER
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.ext.name
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object WhitelistCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    private val ipRegex =
        Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

    override fun create() = BrigadierCommand.literalArgumentBuilder("whitelist").then(
        BrigadierCommand.literalArgumentBuilder("add").then(
            BrigadierCommand.requiredArgumentBuilder(
                "ip",
                greedyString()
            ).executes {
                val ip = StringArgumentType.getString(
                    it,
                    "ip"
                )

                if (!ipRegex.matches(ip))
                {
                    it.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_INVALID_IP))
                    return@executes 0
                }

                scope.launch {
                    DbService.addWhitelist(ip)
                    val msg = miniMsg.deserialize(
                        ConfigRoute.MSG_WHITELIST_ADD,
                        Placeholder.component(
                            "ip",
                            Component.text(ip)
                        )
                    )
                    it.source.sendMessage(msg)

                    LOGGER.info(
                        "{} added {} to whitelist",
                        it.source.name(),
                        ip
                    )
                }

                Command.SINGLE_SUCCESS
            })
    ).then(
        BrigadierCommand.literalArgumentBuilder("remove").then(
            BrigadierCommand.requiredArgumentBuilder(
                "ip",
                greedyString()
            ).suggests { _, builder ->
                val whitelisted = DbService.getWhitelisted()
                whitelisted.forEach { builder.suggest(it) }
                builder.buildFuture()
            }.executes {
                val ip = StringArgumentType.getString(
                    it,
                    "ip"
                )
                scope.launch {
                    val result = DbService.removeWhitelist(ip)

                    if (result)
                    {
                        val msg = miniMsg.deserialize(
                            CONFIG.get(ConfigRoute.MSG_WHITELIST_REMOVE),
                            Placeholder.component(
                                "ip",
                                Component.text(ip)
                            )
                        )
                        it.source.sendMessage(msg)
                        LOGGER.info(
                            "{} removed {} from whitelist",
                            it.source.name(),
                            ip
                        )
                    }
                }
                Command.SINGLE_SUCCESS
            })
    ).then(
        BrigadierCommand.literalArgumentBuilder("list").executes { ctx ->
            scope.launch {
                val whitelisted = DbService.getWhitelisted()

                if (whitelisted.isEmpty())
                {
                    ctx.source.sendRichMessage(CONFIG.get(ConfigRoute.MSG_WHITELIST_EMPTY))
                    return@launch
                }

                whitelisted.forEach {
                    val msg = miniMsg.deserialize(
                        ConfigRoute.MSG_WHITELIST_LIST,
                        Placeholder.component(
                            "ip",
                            Component.text(it)
                        )
                    )

                    ctx.source.sendMessage(msg)
                }
            }
            Command.SINGLE_SUCCESS
        })
}