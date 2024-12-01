plugins {
    id("java")
}

base {
    archivesName.set("tabpre-velocity")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    
    // 添加配置文件处理依赖
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.yaml:snakeyaml:2.2")
    
    // 添加其他必要的依赖
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
    }
    
    processResources {
        filesMatching("velocity-plugin.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
} 