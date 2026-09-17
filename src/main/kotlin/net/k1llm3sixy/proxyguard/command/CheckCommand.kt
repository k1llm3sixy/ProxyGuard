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
import net.k1llm3sixy.proxyguard.ext.get
import net.k1llm3sixy.proxyguard.ext.hasPerms
import net.k1llm3sixy.proxyguard.ext.text
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.k1llm3sixy.proxyguard.services.GuardService
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object CheckCommand : BaseCommand<LiteralArgumentBuilder<CommandSource>>()
{
    override fun create() =
        BrigadierCommand.literalArgumentBuilder("check")
            .requires { it.hasPerms("proxyguard.check") }
            .then(
                BrigadierCommand.requiredArgumentBuilder(
                    "ip",
                    greedyString()
                )
                    .executes {
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
                            val detection = GuardService.checkIp(ip)
                            val msg = miniMsg.deserialize(
                                ConfigRoute.MSG_CHECK_IP,
                                TagResolver.builder()
                                    .text(
                                        "ip",
                                        ip
                                    )
                                    .text(
                                        "detection",
                                        detection.name
                                    )
                                    .build()
                            )

                            it.source.sendMessage(msg)
                        }

                        Command.SINGLE_SUCCESS
                    }
            )
}