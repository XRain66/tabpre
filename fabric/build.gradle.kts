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
    withType<AbstractCopyTask>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

loom {
    runs {
        named("client") {
            client()
            configName = "Minecraft Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Minecraft Server"
            ideConfigGenerated(true)
            runDir("run")
        }
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