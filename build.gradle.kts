plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("checkstyle")
    id("jacoco")
    id("com.github.ben-manes.versions") version "0.47.0"
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
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
    }
    
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
    
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
    
    checkstyle {
        toolVersion = "10.12.4"
        configFile = file("config/checkstyle/checkstyle.xml")
        isIgnoreFailures = true
    }
    
    dependencyUpdates {
        checkForGradleUpdate = true
        outputFormatter = "plain"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
} 