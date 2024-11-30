plugins {
    id("java")
    id("checkstyle")
    id("fabric-loom") version "0.8-SNAPSHOT" apply false
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
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
} 