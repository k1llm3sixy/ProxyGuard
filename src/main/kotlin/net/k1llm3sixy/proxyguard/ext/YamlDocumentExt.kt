package net.k1llm3sixy.proxyguard.ext

import dev.dejvokep.boostedyaml.YamlDocument
import net.k1llm3sixy.proxyguard.io.ConfigRoute

fun YamlDocument.get(route: ConfigRoute): String = getString(route.route)
fun YamlDocument.getB(route: ConfigRoute): Boolean = getBoolean(route.route)