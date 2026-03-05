import java.text.SimpleDateFormat
import java.util.Date
import org.gradle.api.tasks.bundling.Jar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://maven.citizensnpcs.co/repo")
    }
}

dependencies {
    compileOnly(fileTree("libs") { include("*.jar") })
    compileOnly("org.spigotmc:spigot:1.21.11-R0.2-SNAPSHOT:remapped-mojang")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.denizenscript:denizen:1.3.1-SNAPSHOT")
}

val buildNumber: String = System.getenv("BUILD_NUMBER") ?: project.property("BUILD_NUMBER") as String
val buildDate: String = SimpleDateFormat("ddMMyyyy").format(Date())
val pluginVersion = "1.0"

group = "isnsest"
version = pluginVersion
description = "denizen-utilities"

tasks.withType<Jar> {
    archiveFileName.set("denizen-utilities-${pluginVersion}.jar")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to pluginVersion))
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.shadowJar {
    archiveClassifier.set("all")
}
tasks.build {
    dependsOn(tasks.jar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
