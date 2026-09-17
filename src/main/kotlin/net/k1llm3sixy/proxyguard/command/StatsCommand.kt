package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import kotlinx.coroutines.launch
import net.k1llm3sixy.proxyguard.ProxyGuard.Companion.scope
import net.k1llm3sixy.proxyguard.ext.deserialize
import net.k1llm3sixy.proxyguard.ext.hasPerms
import net.k1llm3sixy.proxyguard.ext.text
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.services.DbService
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object StatsCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    override fun create() =
        BrigadierCommand.literalArgumentBuilder("stats")
            .requires { it.hasPerms("proxyguard.stats") }
            .executes {
                scope.launch {
                    val (vpn, proxy, whitelist, valid) = DbService.getStats()

                    val msg = miniMsg.deserialize(
                        ConfigRoute.MSG_STATS,
                        TagResolver.builder()
                            .text(
                                "vpn",
                                vpn.toString()
                            )
                            .text(
                                "proxy",
                                proxy.toString()
                            )
                            .text(
                                "whitelist",
                                whitelist.toString()
                            )
                            .text(
                                "valid",
                                valid.toString()
                            )
                            .build()
                    )

                    it.source.sendMessage(msg)
                }
                Command.SINGLE_SUCCESS
            }
}