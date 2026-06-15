package pl.szczerbal.voidcleaner;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {
    private final VoidCleaner plugin;

    public Config(VoidCleaner plugin) {
        this.plugin = plugin;
        loadDefaults();
    }

    private void loadDefaults() {
        FileConfiguration config = plugin.getConfig();

        // World mapping
        Map<String, Object> defaultWorldVoids = new HashMap<>();
        defaultWorldVoids.put("overworld", java.util.List.of("world"));
        defaultWorldVoids.put("nether-end", java.util.List.of("world_nether", "world_the_end"));
        config.addDefault("world-voids", defaultWorldVoids);
        config.addDefault("default-void", "overworld");

        config.addDefault("cleanup-interval-seconds", 300);
        config.addDefault("void-close-delay-seconds", 60);
        config.addDefault("max-void-storage", 5000);

        config.addDefault("clean-warning-times", java.util.List.of(30, 15, 5));
        config.addDefault("open-warning-times",  java.util.List.of(15, 5));
        config.addDefault("close-warning-times", java.util.List.of(15, 5));

        // Globalne ochrony - dzialaja we WSZYSTKICH trybach czyszczenia
        config.addDefault("protect-tamed", true);
        config.addDefault("protect-named", true);

        // Listy czyszczenia - uzyj EntityRule (patrz: EntityRule.java)
        config.addDefault("default-clean", java.util.List.of(
            "minecraft:item",
            "minecraft:arrow",
            "minecraft:spectral_arrow",
            "minecraft:snowball",
            "minecraft:egg",
            "minecraft:ender_pearl",
            "minecraft:potion",
            "minecraft:experience_bottle",
            "minecraft:oak_boat +empty",
            "minecraft:spruce_boat +empty",
            "minecraft:birch_boat +empty",
            "minecraft:jungle_boat +empty",
            "minecraft:acacia_boat +empty",
            "minecraft:dark_oak_boat +empty",
            "minecraft:mangrove_boat +empty",
            "minecraft:cherry_boat +empty",
            "minecraft:bamboo_raft +empty",
            "minecraft:minecart +empty",
            "minecraft:chest_minecart +empty",
            "minecraft:hopper_minecart +empty",
            "minecraft:tnt_minecart +empty",
            "minecraft:furnace_minecart +empty"
        ));

        // Czarna lista dla zwyklego czyszczenia i TPS-emergency
        config.addDefault("default-blacklist", java.util.List.of());

        // Agresywne czyszczenie (niskie TPS / /void fullclean)
        config.addDefault("high-clean", java.util.List.of(
            "minecraft:item",
            // Pociski
            "minecraft:arrow",
            "minecraft:spectral_arrow",
            "minecraft:trident",
            "minecraft:snowball",
            "minecraft:egg",
            "minecraft:ender_pearl",
            "minecraft:potion",
            "minecraft:experience_bottle",
            "minecraft:llama_spit",
            "minecraft:fireball",
            "minecraft:small_fireball",
            "minecraft:wither_skull",
            "minecraft:dragon_fireball",
            "minecraft:shulker_bullet",
            // Lodzi (wszystkie, rowniez zajete)
            "minecraft:oak_boat",
            "minecraft:spruce_boat",
            "minecraft:birch_boat",
            "minecraft:jungle_boat",
            "minecraft:acacia_boat",
            "minecraft:dark_oak_boat",
            "minecraft:mangrove_boat",
            "minecraft:cherry_boat",
            "minecraft:bamboo_raft",
            // Minecarty (wszystkie)
            "minecraft:minecart",
            "minecraft:chest_minecart",
            "minecraft:hopper_minecart",
            "minecraft:tnt_minecart",
            "minecraft:furnace_minecart",
            // Wrogie moby
            "minecraft:zombie",
            "minecraft:skeleton",
            "minecraft:creeper",
            "minecraft:spider",
            "minecraft:cave_spider",
            "minecraft:enderman",
            "minecraft:slime",
            "minecraft:magma_cube",
            "minecraft:blaze",
            "minecraft:witch",
            "minecraft:phantom",
            "minecraft:drowned",
            "minecraft:husk",
            "minecraft:stray",
            "minecraft:pillager",
            "minecraft:vindicator",
            "minecraft:evoker",
            "minecraft:vex",
            "minecraft:ravager",
            "minecraft:zombie_villager",
            "minecraft:zombified_piglin",
            "minecraft:wither_skeleton",
            "minecraft:ghast",
            "minecraft:silverfish",
            "minecraft:endermite",
            "minecraft:guardian",
            "minecraft:shulker"
        ));

        // Czarna lista dla fullclean (/void fullclean)
        config.addDefault("fullclean-blacklist", java.util.List.of(
            "minecraft:villager",    // nigdy nie usuwaj wiesniaków
            "minecraft:iron_golem"   // nigdy nie usuwaj golemów
        ));

        config.addDefault("tps-check-interval-seconds", 30);
        config.addDefault("low-tps-threshold", 15.0);
        config.addDefault("critical-tps-threshold", 10.0);

        config.addDefault("messages.cleanup-warning",       "&cItems will be cleaned in %seconds% seconds!");
        config.addDefault("messages.cleanup-complete",      "&aAll ground items have been sent to the void!");
        config.addDefault("messages.void-opening",          "&eThe void is now open! Use /void to retrieve items!");
        config.addDefault("messages.void-opening-soon",     "&eVoid opens in %seconds% seconds!");
        config.addDefault("messages.void-closing",          "&cThe void will close in %seconds% seconds!");
        config.addDefault("messages.void-closed",           "&cThe void has closed! Remaining items were permanently deleted.");
        config.addDefault("messages.no-permission",         "&cYou don't have permission to use this command!");
        config.addDefault("messages.void-not-open",         "&cThe void is not currently open!");
        config.addDefault("messages.void-already-open",     "&cThe void is already open!");
        config.addDefault("messages.void-already-closed",   "&cThe void is already closed!");
        config.addDefault("messages.void-manual-cleanup",   "&eManual cleanup triggered by %player%!");
        config.addDefault("messages.void-manual-fullclean",  "&eManual FULL cleanup triggered by %player%!");
        config.addDefault("messages.void-manual-open",      "&eVoid opened manually by %player%!");
        config.addDefault("messages.void-manual-close",     "&eVoid closed manually by %player%!");
        config.addDefault("messages.low-tps-cleanup",       "&cLow TPS detected (%tps%)! Running emergency cleanup...");
        config.addDefault("messages.critical-tps-cleanup",  "&4CRITICAL: TPS at %tps%! Running full cleanup...");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    // --- Timery ---

    public int getCleanupInterval() {
        return plugin.getConfig().getInt("cleanup-interval-seconds", 300);
    }

    public int getVoidCloseDelay() {
        return plugin.getConfig().getInt("void-close-delay-seconds", 60);
    }

    public int getMaxVoidStorage() {
        return plugin.getConfig().getInt("max-void-storage", 5000);
    }

    public int[] getCleanWarningTimes() {
        return plugin.getConfig().getIntegerList("clean-warning-times").stream().mapToInt(i -> i).toArray();
    }

    public int[] getOpenWarningTimes() {
        return plugin.getConfig().getIntegerList("open-warning-times").stream().mapToInt(i -> i).toArray();
    }

    public int[] getCloseWarningTimes() {
        return plugin.getConfig().getIntegerList("close-warning-times").stream().mapToInt(i -> i).toArray();
    }

    // --- Globalne ochrony ---

    /** Jesli true: zadne oswojone zwierze nie zostanie usuniete, niezaleznie od list. */
    public boolean shouldProtectTamed() {
        return plugin.getConfig().getBoolean("protect-tamed", true);
    }

    /** Jesli true: zadna encja z nametag nie zostanie usunieta, niezaleznie od list. */
    public boolean shouldProtectNamed() {
        return plugin.getConfig().getBoolean("protect-named", true);
    }

    // --- Reguly czyszczenia ---

    public List<EntityRule> getDefaultCleanRules() {
        return parseRules("default-clean");
    }

    public List<EntityRule> getHighCleanRules() {
        return parseRules("high-clean");
    }

    public List<EntityRule> getDefaultBlacklistRules() {
        return parseRules("default-blacklist");
    }

    public List<EntityRule> getFullcleanBlacklistRules() {
        return parseRules("fullclean-blacklist");
    }

    private List<EntityRule> parseRules(String configPath) {
        return plugin.getConfig().getStringList(configPath).stream()
                .map(EntityRule::parse)
                .collect(Collectors.toList());
    }

    // --- TPS ---

    public double getLowTpsThreshold() {
        return plugin.getConfig().getDouble("low-tps-threshold", 15.0);
    }

    public double getCriticalTpsThreshold() {
        return plugin.getConfig().getDouble("critical-tps-threshold", 10.0);
    }

    public int getTpsCheckInterval() {
        return plugin.getConfig().getInt("tps-check-interval-seconds", 30);
    }

    // --- Wiadomosci ---

    public String getMessage(String path) {
        String raw = plugin.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getMessage(String path, String placeholder, String value) {
        return getMessage(path).replace(placeholder, value);
    }

    // --- World Void Mapping ---

    /** Zwraca void ID dla danego świata. Jeśli świat nie jest zmapowany, zwraca default-void. */
    public String getVoidIdForWorld(String worldName) {
        Map<String, List<String>> worldVoids = getWorldVoidMapping();
        for (Map.Entry<String, List<String>> entry : worldVoids.entrySet()) {
            if (entry.getValue().contains(worldName)) {
                return entry.getKey();
            }
        }
        return plugin.getConfig().getString("default-void", "overworld");
    }

    private Map<String, List<String>> getWorldVoidMapping() {
        Map<String, List<String>> result = new HashMap<>();
        var worldVoidsSection = plugin.getConfig().getConfigurationSection("world-voids");
        if (worldVoidsSection != null) {
            for (String voidId : worldVoidsSection.getKeys(false)) {
                List<String> worlds = plugin.getConfig().getStringList("world-voids." + voidId);
                result.put(voidId, worlds);
            }
        }
        return result;
    }

    /** Zwraca wszystkie dostępne void IDs. */
    public List<String> getAllVoidIds() {
        return new ArrayList<>(getWorldVoidMapping().keySet());
    }
}
