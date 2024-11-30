plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

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
        name = "aliyun"
        url = uri("https://maven.aliyun.com/repository/public")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
} 