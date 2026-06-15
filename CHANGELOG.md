# Changelog

All notable changes to this project are documented here.

## [1.0.0] - 2026

### Added
- ✨ **Intelligent TPS-Based Cleanup** - System automatically adjusts cleanup aggression based on server load
- 📦 **Multi-World Void Storage** - Separate void storage for different world groups
- ⚡ **Async World Cleanup** - `/void worldclean` command for cleaning unloaded chunks without lag
- 🔄 **Real-Time Player Synchronization** - All players see item changes instantly
- 📊 **Smart Item Stacking** - Automatic merging and respecting of max stack sizes
- 🛡️ **Advanced Entity Filtering** - Customizable cleanup rules with conditions
- 🔐 **Granular Permissions** - Per-command permission nodes
- 🌍 **Multi-Language Support** - Configurable messages

### Fixed
- 🐛 **Memory Leak** - Fixed disconnect cleanup not removing player data
- 🐛 **Player Sync** - Fixed items not updating for other players
- 🐛 **Stack Size** - Fixed incorrect stacking for items with max size < 64
- 🐛 **Thread Safety** - Fixed async operations on main-thread-only objects
- 🐛 **Lambda Syntax** - Fixed BukkitRunnable lambda compilation errors

### Technical
- Uses Paper 1.20 API
- Optimized for performance
- No external dependencies
- Tested on 1.20+

## Future Roadmap

- [ ] Admin GUI for void management
- [ ] Particle effects on void open
- [ ] Sound effects for notifications
- [ ] Bossbar showing void status
- [ ] Custom void names per group
- [ ] Void statistics and analytics
- [ ] Automatic world detection

## Version History

### 1.0.0
Initial release with core functionality.

---

**For detailed information, see [README.md](README.md) and [INSTALLATION.md](INSTALLATION.md)**
