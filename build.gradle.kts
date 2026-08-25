plugins {
    java
    id("com.gradleup.shadow") version "9.3.1"
}

val pluginVersion = "2.9.0"

allprojects {
    group = "com.isnsest"
    version = pluginVersion

    apply(plugin = "java-library")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.citizensnpcs.co/repo")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://nexus.scarsz.me/content/groups/public/")
        maven("https://repo.viaversion.com")
        maven("https://repo.codemc.io/repository/maven-releases/")
    }
}

subprojects {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
        options.encoding = "UTF-8"
    }

    tasks.processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("**/*plugin.yml") {
            expand(props)
        }
    }
}

tasks.shadowJar {
    archiveFileName.set("denizen-utilities-$pluginVersion.jar")
    archiveClassifier.set("")
    subprojects.forEach { subproject ->
        from(subproject.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main").output)
    }
    configurations = subprojects.map { it.configurations.getByName("runtimeClasspath") }
}