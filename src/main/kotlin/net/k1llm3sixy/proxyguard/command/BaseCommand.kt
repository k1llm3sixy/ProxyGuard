package net.k1llm3sixy.proxyguard.command

import net.kyori.adventure.text.minimessage.MiniMessage

abstract class BaseCommand<T>
{
    protected val miniMsg = MiniMessage.miniMessage()

    abstract fun create(): T
}