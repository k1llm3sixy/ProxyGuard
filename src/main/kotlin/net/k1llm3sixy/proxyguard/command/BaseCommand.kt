package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.MiniMessage

abstract class BaseCommand
{
    protected val miniMsg = MiniMessage.miniMessage()

    abstract fun create(): LiteralArgumentBuilder<CommandSource>
}