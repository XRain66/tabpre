plugins {
    id("fabric-loom") version "1.2.7"
    id("maven-publish")
}

base {
    archivesName.set(project.property("archives_base_name").toString())
}

version = project.property("mod_version").toString()
group = project.property("maven_group").toString()

println("Minecraft version: " + project.property("minecraft_version"))
println("Fabric API version: " + project.property("fabric_version"))
println("Loader version: " + project.property("loader_version"))

repositories {
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "Mojang"
        url = uri("https://libraries.minecraft.net/")
    }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    
    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks {
    processResources {
        inputs.property("version", project.version)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

tasks.register("printConfig") {
    doLast {
        println("=== Build Configuration ===")
        println("Project version: $version")
        println("Group: $group")
        println("Archive name: ${base.archivesName.get()}")
        println("Java version: ${java.targetCompatibility}")
        println("=== Dependencies ===")
        println("Minecraft: ${project.property("minecraft_version")}")
        println("Fabric API: ${project.property("fabric_version")}")
        println("Loader: ${project.property("loader_version")}")
        println("Yarn mappings: ${project.property("yarn_mappings")}")
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