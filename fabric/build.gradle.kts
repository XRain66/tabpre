plugins {
    id("fabric-loom")
}

base {
    archivesName.set("tabpre-fabric")
}

group = "com.example.tabprefabric"

dependencies {
    minecraft("com.mojang:minecraft:1.17.1")
    mappings("net.fabricmc:yarn:1.17.1+build.65:v2")
    modImplementation("net.fabricmc:fabric-loader:0.11.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.0+1.17")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
            include("com/example/tabprefabric/**")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
}

loom {
    runs {
        server {
            property("mixin.debug", "true")
            property("mixin.debug.export", "true")
        }
    }
    
    mixin {
        defaultRefmapName.set("tabpre.refmap.json")
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
    
    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
    
    compileJava {
        options.encoding = "UTF-8"
        source = sourceSets.main.get().java.sourceDirectories
    }
}

java {
    withSourcesJar()
} 