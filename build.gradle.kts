buildscript {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:0.8-SNAPSHOT")
    }
}

plugins {
    id("java")
    id("checkstyle")
}

allprojects {
    apply(plugin = "java")
    
    group = "com.example"
    version = "1.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "spongepowered"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-sourcepath")
        options.compilerArgs.add(sourceSets.main.get().java.srcDirs.joinToString(File.pathSeparator))
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
} 