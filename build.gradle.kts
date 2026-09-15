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

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0")

    implementation("dev.dejvokep:boosted-yaml:1.3.6")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")

    implementation("org.bstats:bstats-velocity:3.2.1")
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

    relocate(
        "org.bstats",
        project.group.toString()
    )
}

tasks {
    build { dependsOn(shadowJar) }

    runServer {
        minecraftVersion("26.2")
        jvmArgs(
            "-Xms1G",
            "-Xmx1G",
            "-Dcom.mojang.eula.agree=true"
        )

        runDirectory.set(file("run-paper"))
    }

    runVelocity {
        velocityVersion("4.1.2-SNAPSHOT")
        jvmArgs(
            "-Xms512M",
            "-Xmx512M",
        )

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
