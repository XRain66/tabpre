plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    // Velocity API
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    
    // 添加 Guice 依赖
    compileOnly("com.google.inject:guice:5.1.0")
    
    // 添加 SLF4J 依赖
    compileOnly("org.slf4j:slf4j-api:1.7.32")
    
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
        archiveBaseName.set("tabpre-velocity")
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
    
    build {
        dependsOn(shadowJar)
    }
} 