package net.k1llm3sixy.proxyguard.command

import com.velocitypowered.api.command.BrigadierCommand

object ProxyGuardCommand : BaseCommand<BrigadierCommand>()
{
    override fun create() = BrigadierCommand(
        BrigadierCommand.literalArgumentBuilder("proxyguard")
            .then(ReloadCommand.create())
            .then(UsersCommand.create())
            .then(UnbanCommand.create())
            .then(WhitelistCommand.create())
            .build()
    )
}