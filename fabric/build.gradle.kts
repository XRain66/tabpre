plugins {
    id("fabric-loom")
    id("java")
}

base {
    archivesName.set("tabpre-fabric")
}

group = "com.example.tabprefabric"
version = rootProject.version

dependencies {
    minecraft("com.mojang:minecraft:1.17.1")
    mappings("net.fabricmc:yarn:1.17.1+build.65:v2")
    modImplementation("net.fabricmc:fabric-loader:0.11.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.0+1.17")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

sourceSets {
    main {
        java {
            srcDirs("${projectDir}/src/main/java")
            include("com/example/tabprefabric/**")
        }
        resources {
            srcDirs("${projectDir}/src/main/resources")
        }
    }
}

loom {
    runs {
        named("client") {
            property("mixin.debug", "true")
            property("mixin.debug.export", "true")
            property("mixin.refmap", "tabpre.refmap.json")
        }
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
        sourceCompatibility = "17"
        targetCompatibility = "17"
        
        source = sourceSets.main.get().java.sourceDirectories
        
        options.compilerArgs.addAll(listOf(
            "-sourcepath", sourceSets.main.get().java.asPath,
            "-proc:none"
        ))
    }
}

// 添加 idea 插件配置
apply(plugin = "idea")

idea {
    module {
        sourceDirs.add(file("src/main/java"))
        generatedSourceDirs.add(file("src/main/java"))
    }
} 