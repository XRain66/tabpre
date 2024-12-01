plugins {
    id("java")
}

base {
    archivesName.set("tabpre-velocity")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    
    processResources {
        filesMatching("velocity-plugin.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
} 