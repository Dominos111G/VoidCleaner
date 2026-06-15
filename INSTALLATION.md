# 📥 Installation Guide

## Requirements

- **Minecraft Server:** Paper 1.20+
- **Java:** Java 17+
- **Permissions:** Access to `plugins/` folder

## Method 1: Modrinth (Recommended)

### Using a Launcher (Prism Launcher, MultiMC)

1. Open your launcher
2. Go to "Mods" tab
3. Click "Add Mods"
4. Search for "VoidCleaner"
5. Click "Select mod for download"
6. Wait for download

### Manual from Modrinth

1. Visit https://modrinth.com/plugin/voidcleaner
2. Download the `.jar` file
3. Place in `plugins/` folder
4. Restart server

## Method 2: GitHub

### Download from Releases

1. Visit https://github.com/Dominos111G/VoidCleaner/releases
2. Download the latest `.jar`
3. Place in `plugins/` folder
4. Restart server

### Build from Source

```bash
# Clone repository
git clone https://github.com/Dominos111G/VoidCleaner.git
cd VoidCleaner

# Build the plugin
./gradlew build

# JAR will be in: build/libs/VoidCleaner-*.jar
```

## Method 3: Manual Installation

1. **Download JAR**
   - From Modrinth: https://modrinth.com/plugin/voidcleaner
   - From GitHub: https://github.com/Dominos111G/VoidCleaner/releases

2. **Place in plugins folder**
   ```
   your-server/
   └── plugins/
       └── VoidCleaner.jar
   ```

3. **Start the server**
   ```bash
   java -Xmx2G -Xms2G -jar paper.jar nogui
   ```

4. **Config auto-generates**
   ```
   your-server/
   └── plugins/
       └── VoidCleaner/
           └── config.yml
   ```

5. **Edit config** (optional)
   - Open `plugins/VoidCleaner/config.yml`
   - Customize settings
   - Restart server

## Post-Installation Setup

### 1. Verify Plugin Loaded

You should see in console:
```
[XX:XX:XX] [Server thread/INFO]: [VoidCleaner] Loading VoidCleaner v1.0.0
[XX:XX:XX] [Server thread/INFO]: [VoidCleaner] VoidCleaner has been enabled!
```

### 2. Test Basic Commands

```
/void                    # Open GUI (if void is open)
/void clean              # Test admin command
/void fullclean          # Test full cleanup
```

### 3. Set Permissions

If using a permission plugin (e.g., LuckPerms):

```
# Give players access to void GUI
/lp user <player> permission set voidcleaner.use true

# Give admin all commands
/lp user <admin> permission set voidcleaner.admin true
```

### 4. Customize Config (Optional)

Edit `plugins/VoidCleaner/config.yml`:

```yaml
# Increase storage capacity
max-void-storage: 20000

# Change cleanup interval
cleanup-interval-seconds: 300

# Adjust TPS thresholds
low-tps-threshold: 12.0
critical-tps-threshold: 8.0
```

## Configuration Recommendations

### Small Server (2-4 players)
```yaml
max-void-storage: 5000
cleanup-interval-seconds: 300
low-tps-threshold: 15.0
critical-tps-threshold: 10.0
```

### Medium Server (5-20 players)
```yaml
max-void-storage: 10000
cleanup-interval-seconds: 600
low-tps-threshold: 12.0
critical-tps-threshold: 8.0
```

### Large Server (20+ players)
```yaml
max-void-storage: 20000
cleanup-interval-seconds: 1200
low-tps-threshold: 10.0
critical-tps-threshold: 6.0
```

## Troubleshooting

### Plugin didn't load

**Check:**
1. Are you using Paper 1.20+?
   ```
   /version
   ```
2. Is the JAR in `plugins/` folder?
3. Do you have Java 17+?
   ```
   java -version
   ```
4. Check console for error messages

**Solution:**
- Download correct Paper version
- Verify Java installation
- Check file permissions

### "/void" command unknown

**Check:**
1. Did the plugin load? (Look for "[VoidCleaner]" in console)
2. Do you have `voidcleaner.use` permission?
3. Is the server properly restarted?

**Solution:**
- Restart server completely
- Check permissions with: `/perms check voidcleaner.use`
- Verify plugin.yml is valid

### Void won't open

**Check:**
1. Is `enable-auto-cleanup: true` in config?
2. Has cleanup interval passed?
3. Are there items to store?

**Solution:**
```
# Force cleanup to test
/void clean

# Or manually open
/void open

# Check config values
cleanup-interval-seconds: 600
void-close-delay-seconds: 120
```

### Items disappearing

**Check:**
1. Is void full? (`max-void-storage: 10000`)
2. Are items protected in config?

**Solution:**
```yaml
# Protect all items
fullclean-blacklist:
  - minecraft:item

# Or increase storage
max-void-storage: 20000
```

### High server lag

**Solutions:**
1. Increase cleanup interval
   ```yaml
   cleanup-interval-seconds: 1200  # 20 min instead of 10
   ```

2. Reduce cleanup rules
   ```yaml
   default-clean:
     - minecraft:item           # Keep only essential rules
     - minecraft:arrow
   ```

3. Increase TPS check interval
   ```yaml
   tps-check-interval-seconds: 60  # 60 sec instead of 30
   ```

## Uninstalling

1. **Stop the server**
2. **Remove JAR**
   ```bash
   rm plugins/VoidCleaner.jar
   ```
3. **Remove config** (optional)
   ```bash
   rm -r plugins/VoidCleaner/
   ```
4. **Start the server**

## Updating

### Using Launcher

The launcher will notify you of updates automatically.

### Manual Update

1. **Download new version**
2. **Stop the server**
3. **Replace JAR**
   ```bash
   mv VoidCleaner-new.jar plugins/VoidCleaner.jar
   ```
4. **Restart the server**

**Note:** Config is not overwritten, so your settings are preserved.

## Migrating from Other Plugins

If you're switching from another item cleanup plugin:

1. **Backup config**
   ```bash
   cp plugins/VoidCleaner/config.yml plugins/VoidCleaner/config.yml.bak
   ```

2. **Customize config to match your old plugin**
   - Adjust cleanup rules
   - Set TPS thresholds
   - Configure world mapping

3. **Test on a test server first**

4. **Deploy to production**

## Performance Tips

### For Low-End Servers

```yaml
# Reduce frequency
cleanup-interval-seconds: 1200

# Reduce TPS check
tps-check-interval-seconds: 60

# Fewer cleanup rules
default-clean:
  - minecraft:item

# Disable async cleanup
# (Don't use /void worldclean)
```

### For High-End Servers

```yaml
# More aggressive
cleanup-interval-seconds: 300
low-tps-threshold: 15.0

# More rules
default-clean:
  - minecraft:item
  - minecraft:arrow
  - minecraft:potion
  # ... all rules

# Can safely use /void worldclean
```

## Multi-Server Setup

If running multiple servers with same config:

1. Create a shared config template
2. Copy to each server's `plugins/VoidCleaner/`
3. Adjust world names in each copy
4. Restart each server

## Getting Help

- **Issues:** https://github.com/Dominos111G/VoidCleaner/issues
- **Discord:** [Community Discord Link]
- **Wiki:** https://github.com/Dominos111G/VoidCleaner/wiki

---

**Happy cleaning!** 🚀
