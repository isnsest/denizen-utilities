plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    compileOnly("com.denizenscript:denizen:1.3.2-SNAPSHOT")

    compileOnly("net.skinsrestorer:skinsrestorer-api:15.11.0")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.0.1")
    compileOnly("com.discordsrv:discordsrv:1.28.0")
    compileOnly("com.viaversion:viaversion-api:5.11.0")
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.6.1")

    implementation("com.github.retrooper:packetevents-spigot:2.13.0")

    compileOnly(fileTree(projectDir.resolve("libs")) { include("*.jar") })
    paperweight.paperDevBundle("26.1.2.build.+")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}