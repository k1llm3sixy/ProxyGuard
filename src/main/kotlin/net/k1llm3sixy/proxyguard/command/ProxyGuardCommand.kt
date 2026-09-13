package net.k1llm3sixy.proxyguard.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource

object ProxyGuardCommand : BaseCommand()
{
    override fun create(): LiteralArgumentBuilder<CommandSource>
    {
        val node =
            BrigadierCommand.literalArgumentBuilder("proxyguard").requires { it.hasPermission("proxyguard.admin") }
                .then(ReloadCommand.create())
                .then(SettingsCommand.create())
                .then(UsersCommand.create())
                .then(UnbanCommand.create())
                .then(WhitelistCommand.create())

        return node
    }
}