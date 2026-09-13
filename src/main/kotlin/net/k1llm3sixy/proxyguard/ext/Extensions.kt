package net.k1llm3sixy.proxyguard.ext

import dev.dejvokep.boostedyaml.YamlDocument
import net.k1llm3sixy.proxyguard.io.ConfigRoute
import net.k1llm3sixy.proxyguard.io.Storage.CONFIG
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

fun YamlDocument.get(route: ConfigRoute): String = getString(route.route)
fun YamlDocument.getB(route: ConfigRoute): Boolean = getBoolean(route.route)

fun MiniMessage.deserialize(route: ConfigRoute, resolver: TagResolver) = deserialize(
    CONFIG.get(route),
    resolver
)

fun TagResolver.Builder.text(key: String, value: String): TagResolver.Builder =
    tag(
        key,
        Tag.selfClosingInserting(Component.text(value))
    )
