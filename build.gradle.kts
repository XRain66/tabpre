plugins {
    id("fabric-loom") version "1.1-SNAPSHOT" apply false
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