package net.k1llm3sixy.proxyguard.command

import net.kyori.adventure.text.minimessage.MiniMessage

abstract class BaseCommand<T>
{
    protected val ipRegex =
        Regex("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
    protected val miniMsg = MiniMessage.miniMessage()

    abstract fun create(): T
}