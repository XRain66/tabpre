plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
    id("fabric-loom") version "1.2.7" apply false
}

subprojects {
    apply(plugin = "java")
    
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(16)
    }
}

tasks.named("clean") {
    doLast {
        layout.buildDirectory.asFile.get().deleteRecursively()
    }
}

tasks.named("build") {
    dependsOn(":velocity:build", ":fabric:build")
} 