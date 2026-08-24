plugins {
    `java-library`
}

dependencies {
    implementation(project(":core"))
    compileOnly(project(":v1_21"))
    compileOnly("org.spigotmc:spigot:26.2-R0.1-SNAPSHOT")
    compileOnly("com.denizenscript:denizen:1.3.3-SNAPSHOT")
    compileOnly(fileTree(projectDir.resolve("libs")) { include("*.jar") })
}