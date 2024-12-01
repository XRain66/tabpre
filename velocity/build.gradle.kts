plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

base {
    archivesName.set("tabpre-velocity")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.yaml:snakeyaml:2.2")
    
    compileOnly("com.google.inject:guice:5.1.0")
    compileOnly("org.slf4j:slf4j-api:1.7.32")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.isIncremental = true
        options.isFork = true
    }
    
    processResources {
        filesMatching("velocity-plugin.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
    
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    shadowJar {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.spongepowered:configurate-core:4.1.2"))
            include(dependency("org.spongepowered:configurate-yaml:4.1.2"))
            include(dependency("org.yaml:snakeyaml:2.2"))
        }
        mergeServiceFiles()
        minimize()
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

// 优化构建性能
gradle.projectsEvaluated {
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xmx1g")
        options.compilerArgs.add("-Xms512m")
    }
}