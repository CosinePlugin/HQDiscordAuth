plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "kr.hqservice.auth"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    compileOnly("com.github.MinseoServer", "MS-Core", "1.0.18")
    compileOnly("org.spigotmc", "spigot", "1.12.2-R0.1-SNAPSHOT")
    implementation("net.dv8tion", "JDA", "5.0.0-beta.8")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveFileName.set("${rootProject.name}-${project.version}.jar")
    destinationDirectory.set(File("D:\\서버\\1.19.3 - 개발\\plugins"))
}