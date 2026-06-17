# 🪐 VoidCleaner

**Intelligent item management plugin for Paper Minecraft 1.20+ servers**

VoidCleaner automatically and intelligently cleans up dropped items, projectiles, vehicles, and mobs from your server, storing them in a special "void" (storage) where players can retrieve them. The system automatically adjusts cleanup aggression based on server load (TPS).

## ✨ Key Features

### 🎯 Intelligent TPS-Based Cleanup
- **Healthy TPS** → Normal cleanup (items, projectiles, empty vehicles)
- **Low TPS** → Aggressive cleanup (+ mobs)
- **Critical TPS** → Full cleanup (almost everything)

### 📦 Multi-World Void Storage
- Separate void for each world group (Overworld, Nether+End, Custom)
- Items automatically route to the correct void
- Flexible world-to-void mapping in config

### ⚡ Asynchronous Cleanup
- `/void worldclean` — Cleans unloaded chunks without lag
- Background scanning, main-thread removal
- Safe for all operations

### 🔄 Real-Time Player Synchronization
- When player A takes an item, player B sees it instantly
- GUI automatically updates for all viewers
- No duplicate or ghost items

### 📊 Smart Item Stacking
- Automatically merges items of the same type
- Respects max stack size (boats=1, iron=64)
- Reduces void slots needed

### 🛡️ Advanced Filtering
- Protects tamed animals (default)
- Protects named entities (default)
- Blacklists for each cleanup mode
- Advanced conditions (+empty, !tamed, +adult, etc.)

## 📥 Installation

1. Download `.jar` from GitHub or Modrinth
2. Place in `plugins/` folder
3. Restart server
4. Edit `plugins/VoidCleaner/config.yml` (auto-generated)
5. Restart server again

See [INSTALLATION.md](INSTALLATION.md) for detailed instructions.

## 🎮 Player Commands

```
/void
```
Opens the void GUI to retrieve items (if void is open).

## 🔧 Admin Commands

```
/void open           # Manually open void
/void close          # Manually close void
/void clean          # Normal cleanup
/void fullclean      # Aggressive cleanup (removes mobs)
/void worldclean     # Async world cleanup (unloaded chunks)
```

## 🔐 Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `voidcleaner.use` | true | Access to `/void` GUI |
| `voidcleaner.admin` | op | All admin commands |
| `voidcleaner.open` | op | `/void open` |
| `voidcleaner.close` | op | `/void close` |
| `voidcleaner.clean` | op | `/void clean` |
| `voidcleaner.fullclean` | op | `/void fullclean` |
| `voidcleaner.worldclean` | op | `/void worldclean` |

### Example: LuckPerms
```
/lp user <player> permission set voidcleaner.use true
/lp group admin permission set voidcleaner.admin true
```

## ⚙️ Configuration

### World Mapping

```yaml
world-voids:
  overworld:
    - world
  nether-end:
    - world_nether
    - world_the_end

default-void: overworld
```

Items from each world go to their mapped void.

### Storage Limit

```yaml
max-void-storage: 10000  # Max items per void (0 = unlimited)
```

### Auto Cleanup

```yaml
cleanup-interval-seconds: 600  # Cleanup every 10 minutes
enable-auto-cleanup: true
```

**Recommendations:**
- Small server (2-4 players): 300s (5 min)
- Medium server (5-20 players): 600s (10 min)
- Large server (20+ players): 1200s (20 min)

### TPS-Based Cleanup

```yaml
low-tps-threshold: 12.0          # Trigger aggressive cleanup
critical-tps-threshold: 8.0      # Trigger full cleanup
tps-check-interval-seconds: 30
```

**How it works:**
- TPS > 12.0 → Normal mode
- TPS 8.0-12.0 → Remove mobs too
- TPS < 8.0 → Full cleanup

### Void Duration

```yaml
void-close-delay-seconds: 120  # Void closes after 2 minutes
```

After this time, the void closes and remaining items are deleted forever.

### Cleanup Rules

```yaml
default-clean:  # Regular cleanup
  - minecraft:item
  - minecraft:arrow
  - minecraft:oak_boat +empty

high-clean:     # Aggressive cleanup
  - minecraft:zombie
  - minecraft:skeleton
  # ... more mobs
```

#### Rule Format

```
"minecraft:item"              # All items
"minecraft:oak_boat +empty"   # Empty boats only
"minecraft:minecart"          # All minecarts
"minecraft:wolf !tamed"       # Wild wolves only
"minecraft:zombie +adult"     # Adult zombies only
```

#### Conditions

| Condition | Meaning |
|-----------|---------|
| `+empty` | Vehicle without passenger |
| `!empty` | Vehicle with passenger |
| `+tamed` | Tamed animal |
| `!tamed` | Wild animal |
| `+named` | Has nametag |
| `!named` | No nametag |
| `+adult` | Adult |
| `+baby` | Baby/young |
| `+hostile` | Hostile mob |
| `!hostile` / `+passive` | Peaceful |
| `+on_ground` | Standing on ground |

### Blacklists

```yaml
default-blacklist: []        # Protected from default cleanup
fullclean-blacklist:
  - minecraft:villager       # Never removed
  - minecraft:iron_golem
```

See full [config.yml](src/main/resources/config.yml) for all options.

## 📊 Use Cases

### Small SMP Server

```yaml
world-voids:
  all:
    - world
    - world_nether
    - world_the_end

max-void-storage: 5000
cleanup-interval-seconds: 300
```

### Large Survival Server

```yaml
world-voids:
  main:
    - world
    - world_nether
    - world_the_end
  creative:
    - creative_world

max-void-storage: 20000
cleanup-interval-seconds: 600
low-tps-threshold: 12.0
```

### Anti-Lag Machine Setup

```yaml
enable-auto-cleanup: false
protect-tamed: true
protect-named: true
```

Then manually run `/void worldclean` to clean unloaded areas.

## 🐛 Troubleshooting

### Plugin won't load

**Check:**
1. Are you using Paper 1.20+? (`/version`)
2. Is the JAR in the `plugins/` folder?
3. Do you have Java 17+? (`java -version`)

### `/void` command not found

**Check:**
1. Did the plugin load? (Check console for "[VoidCleaner]")
2. Do you have `voidcleaner.use` permission?
3. Restart the server

### Void won't open

**Check:**
1. Run `/void fullclean` to force cleanup
2. Check `void-close-delay-seconds` in config
3. Is `enable-auto-cleanup: true`?

### High CPU usage

**Solutions:**
- Increase `cleanup-interval-seconds` (e.g., 1200)
- Increase `tps-check-interval-seconds` (e.g., 60)
- Reduce rules in `default-clean`

See [INSTALLATION.md](INSTALLATION.md) for more solutions.

## 📈 Performance

VoidCleaner is optimized for performance:

- ✅ Async world scanning (no lag)
- ✅ Efficient entity filtering
- ✅ Memory-optimized storage
- ✅ Smart stacking reduces slots
- ✅ Main-thread safe operations

**TPS Impact:**
- Regular cleanup: < 0.1 TPS drop
- Void open/close: < 0.01 TPS drop
- Player sync: No impact

## 🔁 Version History
### Future Roadmap
- [ ] Admin GUI for void management
- [ ] Particle effects on void open
- [ ] Sound effects for notifications
- [ ] Bossbar showing void status
- [ ] Custom void names per group
- [ ] Void statistics and analytics
- [ ] Automatic world detection

**0.1.0**
- Initial release with core functionality.

**0.1.1**
- Added support for 1.21.x,
- Cleaned code and fixed multi-language comments in code.


## 🔗 Links

- **GitHub:** https://github.com/Dominos111G/VoidCleaner
- **Modrinth:** https://modrinth.com/plugin/voidcleaner
- **Issues:** https://github.com/Dominos111G/VoidCleaner/issues

## 📝 License

MIT License - Free to modify and distribute

## 📖 Documentation

- [INSTALLATION.md](INSTALLATION.md) - Detailed setup guide
- [CONFIGURATION.md](README.md#-configuration) - Config options
- [CONTRIBUTING.md](CONTRIBUTING.md) - Developer guide
- [CHANGELOG.md](CHANGELOG.md) - Version history

## 💬 Support

Found a bug or want a feature? [Open an issue](https://github.com/Dominos111G/VoidCleaner/issues)!

---

**Version:** 1.0.0  
**Minecraft:** 1.20+  
**Made with ❤️ for Minecraft Players**
