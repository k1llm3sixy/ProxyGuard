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

    implementation("dev.dejvokep:boosted-yaml:1.3.6")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.11.0")
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

tasks {
    build {
        dependsOn(shadowJar)
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
        }
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}
