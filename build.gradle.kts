buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.3.+")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

allprojects {
    version = "1.0.0"
    
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}

subprojects {
    apply(plugin = "java")
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
} 