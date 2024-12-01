pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("net.fabricmc")
                includeGroup("net.fabricmc.fabric-api")
                includeGroup("fabric-loom")
            }
        }
        maven("https://cursemaven.com") {
            name = "CurseMaven"
        }
        maven("https://jitpack.io") {
            name = "JitPack"
        }
        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://cursemaven.com") {
            name = "CurseMaven"
        }
        maven("https://jitpack.io") {
            name = "JitPack"
        }
        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"
        }
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "tabpre"

include("velocity")
include("fabric") 