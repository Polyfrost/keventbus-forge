plugins {
    kotlin("jvm") version "1.9.10"
    id("org.polyfrost.loom") version "1.6.polyfrost.5"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    java
    id("org.polyfrost.defaults.maven-publish") version "0.6.5"
    id("maven-publish")
}

//Constants:

val baseGroup: String by project
val version: String by project

group = baseGroup
project.version = version

// Toolchains:
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withSourcesJar()
}

// Minecraft configuration:
loom {
    runConfigs {
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

sourceSets.main {
    output.setResourcesDir(java.classesDirectory)
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8", "1.5.0"))

    testImplementation("junit:junit:4.12")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("com.github.deamsy:eventbus:1.1")
    testImplementation("com.google.guava:guava:29.0-jre")
    testImplementation("org.testng:testng:7.1.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}