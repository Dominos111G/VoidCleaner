plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "pl.szczerbal.voidcleaner"
// Version is defined in gradle.properties

// ============================================================
// Version Configuration
// ============================================================
val targetVersion = project.properties["mcVersion"]?.toString() ?: "1.20.6"
val isShadowJar = project.properties["shadow"]?.toString()?.toBoolean() ?: false

// Supported Paper Minecraft versions (1.20.x and 1.21.x)
val supportedVersions = mapOf(
    "1.20.1" to "1.20.1-R0.1-SNAPSHOT",
    "1.20.2" to "1.20.2-R0.1-SNAPSHOT",
    "1.20.3" to "1.20.3-R0.1-SNAPSHOT",
    "1.20.4" to "1.20.4-R0.1-SNAPSHOT",
    "1.20.5" to "1.20.5-R0.1-SNAPSHOT",
    "1.20.6" to "1.20.6-R0.1-SNAPSHOT",

    "1.21.0" to "1.21.0-R0.1-SNAPSHOT",
    "1.21.1" to "1.21.1-R0.1-SNAPSHOT",
    "1.21.2" to "1.21.2-R0.1-SNAPSHOT",
    "1.21.3" to "1.21.3-R0.1-SNAPSHOT",
    "1.21.4" to "1.21.4-R0.1-SNAPSHOT",
    "1.21.5" to "1.21.5-R0.1-SNAPSHOT",
    "1.21.6" to "1.21.6-R0.1-SNAPSHOT",
    "1.21.7" to "1.21.7-R0.1-SNAPSHOT",
    "1.21.8" to "1.21.8-R0.1-SNAPSHOT",
    "1.21.9" to "1.21.9-R0.1-SNAPSHOT",
    "1.21.10" to "1.21.10-R0.1-SNAPSHOT",
    "1.21.11" to "1.21.11-R0.1-SNAPSHOT"
)

val selectedApi = supportedVersions[targetVersion]
    ?: supportedVersions["1.20.6"]
    ?: error("Unsupported version: $targetVersion. Supported: ${supportedVersions.keys}")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$selectedApi")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// ============================================================
// JAR Configuration
// ============================================================
tasks {
    jar {
        // For universal build: use "universal" naming
        // For version-specific: use "mc${targetVersion}"
        val suffix = if (targetVersion == "1.21.11") "universal" else "mc${targetVersion}"
        archiveFileName.set("VoidCleaner-${project.version}-${suffix}.jar")
    }

    shadowJar {
        archiveFileName.set("VoidCleaner-${project.version}-mc${targetVersion}-all.jar")
        mergeServiceFiles()
    }

    // ============================================================
    // Server Configuration & Tasks
    // ============================================================
    runServer {
        minecraftVersion(targetVersion)
        jvmArgs(
            "-Xms2G",
            "-Xmx2G",
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:G1NewCollectionPercentage=30",
            "-XX:G1MaxNewGenTaskPercent=40",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapRegionSize=16M"
        )
    }

    // Build single universal JAR for all versions
    register("buildMultiVersion") {
        group = "build"
        description = "Build single universal JAR supporting all versions (1.20.1-1.21.11)"
        notCompatibleWithConfigurationCache("Prints project version at execution time")
        dependsOn("clean", "jar")
        doLast {
            println("\n✓ UNIVERSAL JAR created!")
            println("Supports: 1.20.1 - 1.20.6, 1.21.0 - 1.21.11")
            println("File: build/libs/VoidCleaner-${project.version}-universal.jar")
            println("\nReady for Modrinth multi-version upload!")
        }
    }

    // Build task for all versions (individual JARs)
    register("buildAll") {
        group = "build"
        description = "Build individual JAR for each supported version"
        dependsOn("clean")
        doLast {
            supportedVersions.keys.forEach { version ->
                println("Building for Minecraft $version...")
                Runtime.getRuntime().exec(
                    arrayOf("gradle", "build", "-PmcVersion=$version", "-x", "test")
                ).waitFor()
            }
        }
    }

    // Run test server with latest supported version
    register<JavaExec>("runLatestServer") {
        group = "run"
        description = "Run test server with latest supported version"
        doFirst {
            println("Starting test server with $targetVersion...")
        }
        dependsOn("runServer")
    }

    // Quick dev build without running tests
    register("devBuild") {
        group = "build"
        description = "Quick dev build without tests"
        dependsOn("jar")
        doLast {
            println("✓ Dev build complete: build/libs/VoidCleaner-${project.version}-mc${targetVersion}.jar")
        }
    }

    // Build fat JAR with all dependencies included
    register("fatJar") {
        group = "build"
        description = "Build uber JAR (all dependencies included)"
        dependsOn("shadowJar")
        doLast {
            println("✓ Fat JAR build: build/libs/VoidCleaner-${project.version}-mc${targetVersion}-all.jar")
        }
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to "Intelligent item management plugin",
            "api-version" to "1.20"
        )
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    // ============================================================
    // Information Tasks
    // ============================================================
    register("versions") {
        group = "help"
        description = "Show supported Minecraft versions"
        doLast {
            println("\n=== Supported Minecraft Versions ===")
            supportedVersions.keys.forEach { version ->
                val current = if (version == targetVersion) " ← CURRENT" else ""
                println("  • $version$current")
            }
            println("\nBuild for specific version:")
            println("  ./gradlew build -PmcVersion=1.21.0")
            println("\n")
        }
    }

    register("info") {
        group = "help"
        description = "Show build configuration"
        doFirst {
            println("\n=== VoidCleaner Build Configuration ===")
            println("Target Minecraft Version: $targetVersion")
            println("Paper API: $selectedApi")
            println("Plugin Version: ${project.version}")
            println("Java: 17+")
            println("\n=== Available Tasks ===")
            println("  ./gradlew build              - Build JAR")
            println("  ./gradlew devBuild           - Quick dev build")
            println("  ./gradlew fatJar             - Build with all dependencies")
            println("  ./gradlew buildAll           - Build for all supported versions")
            println("  ./gradlew runServer          - Run test server")
            println("  ./gradlew runLatestServer    - Run with latest version")
            println("  ./gradlew versions           - Show supported versions")
            println("\n=== Building for Different Versions ===")
            println("  ./gradlew build -PmcVersion=1.20.1")
            println("  ./gradlew build -PmcVersion=1.21.0")
            println("\n")
        }
    }
}

// ============================================================
// Print startup info
// ============================================================
gradle.projectsEvaluated {
    println("\n>>> Building VoidCleaner for Minecraft $targetVersion")
}
