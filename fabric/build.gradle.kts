plugins {
    id("fabric-loom") version "1.2.7"
    id("maven-publish")
}

base {
    archivesName.set(project.property("archives_base_name").toString())
}

version = project.property("mod_version").toString()
group = project.property("maven_group").toString()

repositories {
    maven("https://maven.fabricmc.net/") {
        name = "FabricMC"
    }
    maven("https://maven.fabricmc.io/") {
        name = "FabricMaven"
    }
    maven("https://libraries.minecraft.net/") {
        name = "Mojang"
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-networking-api-v1:${project.property("fabric_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:${project.property("fabric_version")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }
    
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
} 