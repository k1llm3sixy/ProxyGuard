import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-velocity") version "3.1.0"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    kotlin("kapt") version "2.4.10"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

val withoutNatives: Configuration = configurations.create("withoutNatives")

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0")

    implementation("dev.dejvokep:boosted-yaml:1.3.6")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")

    withoutNatives("dev.dejvokep:boosted-yaml:1.3.6")
    withoutNatives(files("sqlite-jdbc-3.53.4.0-without-natives.jar"))
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*"))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val noNatives = tasks.register(
    "noNatives",
    ShadowJar::class.java
) {
    group = "shadow"
    description = "shadowJar without sqlite native libraries"

    from(sourceSets.main.get().output)
    configurations = listOf(withoutNatives)

    archiveClassifier.set("without-natives")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*"))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks {
    build {
        dependsOn(
            shadowJar,
            noNatives
        )
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs(
            "-Xms1G",
            "-Xmx1G",
            "-Dcom.mojang.eula.agree=true"
        )

        runDirectory.set(file("run-paper"))

        serverJar(file("paper/paper-26.2-121.jar"))

        downloadPlugins {
            modrinth(
                "mckotlin",
                "IL59nw3O"
            )
        }
    }

    runVelocity {
        velocityVersion("4.1.2-SNAPSHOT")

        downloadPlugins {
            modrinth(
                "mckotlin",
                "jCtTVrP9"
            )
            modrinth(
                "luckperms",
                "tamnmXad"
            )
        }
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}
