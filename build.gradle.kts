plugins {
    java
    id("io.github.goooler.shadow") version "8.1.8"
}

val pluginVersion = "2.9.0"

allprojects {
    group = "com.isnsest"
    version = pluginVersion

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.citizensnpcs.co/repo")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://nexus.scarsz.me/content/groups/public/")
        maven("https://repo.viaversion.com")
    }
}

subprojects {
    apply(plugin = "java-library")
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
}