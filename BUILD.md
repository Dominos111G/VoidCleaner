# 🔨 Build Guide

## Requirements

- **Java:** 17+ ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Git:** [Download](https://git-scm.com/download)
- **Gradle:** Included (via gradlew)

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/Dominos111G/VoidCleaner.git
cd VoidCleaner
```

### 2. Build for Latest Version (1.20.4)
```bash
./gradlew build
```
Output: `build/libs/VoidCleaner-1.0.0-mc1.20.4.jar`

### 3. Run Test Server
```bash
./gradlew runServer
```
Server starts on `localhost:25565` (no auth needed)

## Supported Minecraft Versions

| Version | Status | Build Command |
|---------|--------|---------------|
| 1.20.1  | ✅ Supported | `./gradlew build -PmcVersion=1.20.1` |
| 1.20.2  | ✅ Supported | `./gradlew build -PmcVersion=1.20.2` |
| 1.20.3  | ✅ Supported | `./gradlew build -PmcVersion=1.20.3` |
| 1.20.4  | ✅ **Default** | `./gradlew build` |
| 1.21.0  | ✅ Supported | `./gradlew build -PmcVersion=1.21.0` |
| 1.21.1  | ✅ Supported | `./gradlew build -PmcVersion=1.21.1` |
| 1.21.2  | ✅ Supported | `./gradlew build -PmcVersion=1.21.2` |
| 1.21.3  | ✅ Supported | `./gradlew build -PmcVersion=1.21.3` |

## Build Commands

### Standard Builds

```bash
# Build for default version (1.20.4)
./gradlew build

# Build for specific version
./gradlew build -PmcVersion=1.21.0

# Quick dev build (skip tests)
./gradlew devBuild

# Build with all dependencies included (fat JAR)
./gradlew fatJar
```

### Run Test Server

```bash
# Run server with default version
./gradlew runServer

# Run server with specific version
./gradlew runServer -PmcVersion=1.21.0

# Run latest supported version
./gradlew runLatestServer
```

### Build for All Versions

```bash
# Build JARs for all supported versions
./gradlew buildAll
```

Creates JARs for each version in `build/libs/`.

### Info & Help

```bash
# Show configuration
./gradlew info

# List supported versions
./gradlew versions

# Show all tasks
./gradlew tasks
```

## Gradle Properties

### Default Properties (`gradle.properties`)

```properties
# Default Minecraft version
mcVersion=1.20.4

# Include all dependencies in JAR
shadow=false
```

### Override at Build Time

```bash
./gradlew build -PmcVersion=1.21.0 -Pshadow=true
```

## Testing

### Run Test Server Locally

```bash
./gradlew runServer
```

**What this does:**
1. Builds the plugin
2. Downloads Paper 1.20.4
3. Creates test server in `run/` folder
4. Starts server with plugin loaded
5. Accepts commands: `say hello`, `/void`, etc.

**Connection:**
- Address: `localhost:25565`
- Offline mode (no Microsoft account needed)

### Stop Test Server

Type `stop` in console or Ctrl+C

### Server Data Persistence

Test server data is stored in `run/` folder:
```
run/
├── world/           # World data
├── world_nether/
├── world_the_end/
├── plugins/         # Plugin JARs
│   └── VoidCleaner.jar
└── plugins/VoidCleaner/
    └── config.yml
```

To reset server: `rm -rf run/`

## Build Output

### JAR Files

```
build/libs/
├── VoidCleaner-1.0.0-mc1.20.4.jar     # Standard JAR
├── VoidCleaner-1.0.0-mc1.21.0.jar
├── VoidCleaner-1.0.0-mc1.20.4-all.jar # Fat JAR (with deps)
└── ...
```

### File Sizes

- **Standard JAR:** ~50 KB
- **Fat JAR:** ~2-3 MB (includes dependencies)

## Troubleshooting

### "Gradle command not found"

**Windows:**
```bash
.\gradlew build
```

**Mac/Linux:**
```bash
chmod +x gradlew
./gradlew build
```

### Port 25565 already in use

The test server uses port 25565. If it's busy:

1. Kill existing server:
   ```bash
   # Windows
   netstat -ano | findstr :25565
   taskkill /PID <PID> /F
   
   # Mac/Linux
   lsof -i :25565
   kill -9 <PID>
   ```

2. Or change port in `gradle.properties`:
   ```properties
   serverPort=25566
   ```

### "Unsupported Minecraft version"

If you get this error, the version may not be available on Paper yet.

**Check available versions:**
```bash
./gradlew versions
```

### Build fails with "API not found"

Clear Gradle cache:
```bash
./gradlew clean build --refresh-dependencies
```

### Plugin doesn't load in test server

1. Check `run/logs/latest.log` for errors
2. Verify plugin.yml syntax
3. Try fresh server: `rm -rf run/`

## Development Workflow

### 1. Make Changes

```bash
# Edit code in src/main/java/...
```

### 2. Quick Test

```bash
./gradlew devBuild
# Then copy build/libs/*.jar to your server's plugins/ folder
```

### 3. Run Full Test

```bash
./gradlew clean build
./gradlew runServer
```

### 4. Test Different Version

```bash
./gradlew build -PmcVersion=1.21.0
./gradlew runServer -PmcVersion=1.21.0
```

### 5. Build All Versions

```bash
./gradlew buildAll
```

## IDE Setup

### IntelliJ IDEA

1. Open project
2. Gradle will auto-import
3. Mark `src/main/java` as Sources Root
4. Run `./gradlew build` once to download dependencies

### Eclipse

1. `./gradlew eclipse`
2. Import as existing project

### VS Code

1. Install "Extension Pack for Java"
2. Open folder
3. Gradle extension will detect project

## Continuous Integration

### Local Testing Before Push

```bash
./gradlew clean build -x test
./gradlew buildAll
```

### Pre-Commit Hook

Create `.git/hooks/pre-commit`:

```bash
#!/bin/bash
./gradlew check || exit 1
```

Make executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Performance Tips

### Faster Builds

```bash
# Use daemon (keeps Gradle running)
./gradlew build --daemon

# Parallel build
./gradlew build --parallel
```

### Incremental Build

Gradle caches build outputs. Clean only when needed:

```bash
# Normal build (incremental)
./gradlew build

# Full clean rebuild
./gradlew clean build
```

## Publishing

### Build Release JAR

```bash
./gradlew build -PmcVersion=1.20.4
./gradlew build -PmcVersion=1.21.0
```

Then upload from `build/libs/` to:
- GitHub Releases
- Modrinth
- SpigotMC

### Sign JAR (Optional)

For production, consider signing JAR with your GPG key.

## Resources

- [Gradle Documentation](https://docs.gradle.org/)
- [Paper Documentation](https://docs.papermc.io/)
- [Java 17 Documentation](https://docs.oracle.com/en/java/javase/17/)

---

**Need help?** Open an [Issue](https://github.com/Dominos111G/VoidCleaner/issues) on GitHub!
