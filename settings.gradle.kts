pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }
}

rootProject.name = "tabpre"

include("velocity")
include("fabric") 