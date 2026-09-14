package net.k1llm3sixy.proxyguard.command

import com.velocitypowered.api.command.BrigadierCommand

object ProxyGuardCommand : BaseCommand<BrigadierCommand>()
{
    override fun create() = BrigadierCommand(
        BrigadierCommand.literalArgumentBuilder("proxyguard").requires { it.hasPermission("proxyguard.admin") }
            .then(ReloadCommand.create())
            .then(UsersCommand.create())
            .then(UnbanCommand.create())
            .then(WhitelistCommand.create())
            .build()
    )
}