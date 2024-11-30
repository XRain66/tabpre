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
    
    // 配置文件处理
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.yaml:snakeyaml:2.2") {
        exclude(group = "org.yaml", module = "snakeyaml-engine")
    }
    
    // 测试依赖
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
}

tasks {
    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")
        
        // 添加重复策略
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        
        // 重定位依赖，避免冲突
        relocate("org.spongepowered.configurate", "com.example.velocityplugin.libs.configurate")
        relocate("io.leangen.geantyref", "com.example.velocityplugin.libs.geantyref")
        relocate("org.yaml.snakeyaml", "com.example.velocityplugin.libs.snakeyaml")
        
        // 排除不需要的文件
        minimize()
        mergeServiceFiles()
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
    
    build {
        dependsOn(shadowJar)
    }
    
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    jar {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
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

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java"))
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
        }
    }
} 