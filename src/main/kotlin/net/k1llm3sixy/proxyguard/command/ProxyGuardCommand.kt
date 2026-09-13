package net.k1llm3sixy.proxyguard.command

import com.velocitypowered.api.command.BrigadierCommand

object ProxyGuardCommand
{
    fun create(): BrigadierCommand
    {
        val node =
            BrigadierCommand.literalArgumentBuilder("proxyguard").requires { it.hasPermission("proxyguard.admin") }
                .then(ReloadCommand.create())
                .then(SettingsCommand.create())
                .then(UsersCommand.create())
                .then(UnbanCommand.create())
                .then(WhitelistCommand.create())
                .build()

        return BrigadierCommand(node)
    }
}