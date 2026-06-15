package pl.szczerbal.voidcleaner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCleaner {
    private final VoidCleaner plugin;
    private final Config config;
    private BukkitTask cleanupTask;
    private BukkitTask warningTask;
    private BukkitTask tpsMonitorTask;
    private int timeUntilCleanup;

    public ItemCleaner(VoidCleaner plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        startCleanupSchedule();
        startTpsMonitor();
    }

    // -----------------------------------------------------------------------
    // Harmonogram czyszczenia
    // -----------------------------------------------------------------------

    private void startCleanupSchedule() {
        timeUntilCleanup = config.getCleanupInterval();

        cleanupTask = new BukkitRunnable() {
            @Override public void run() {
                cleanupItems();
                timeUntilCleanup = config.getCleanupInterval();
            }
        }.runTaskTimer(plugin, config.getCleanupInterval() * 20L, config.getCleanupInterval() * 20L);

        warningTask = new BukkitRunnable() {
            @Override public void run() {
                timeUntilCleanup--;
                for (int t : config.getCleanWarningTimes()) {
                    if (timeUntilCleanup == t) {
                        Bukkit.broadcastMessage(
                            config.getMessage("cleanup-warning", "%seconds%", String.valueOf(t)));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // -----------------------------------------------------------------------
    // Metody publiczne
    // -----------------------------------------------------------------------

    /** Zwykle czyszczenie (uruchamiane harmonogramem). */
    public void cleanupItems() {
        cleanupItems(false);
    }

    /**
     * Czyszczenie z wyborem listy.
     * force=false -> default-clean + default-blacklist -> zapisuje do voida i otwiera void
     * force=true  -> high-clean   + default-blacklist -> zapisuje do voida, nie otwiera void
     */
    public void cleanupItems(boolean force) {
        List<EntityRule> rules     = force ? config.getHighCleanRules() : config.getDefaultCleanRules();
        List<EntityRule> blacklist = config.getDefaultBlacklistRules();

        List<Entity> toRemove = collectEntities(rules, blacklist);
        if (toRemove.isEmpty()) return;

        sendItemsToVoid(toRemove);

        for (Entity e : toRemove) e.remove();

        Bukkit.broadcastMessage(config.getMessage("cleanup-complete"));

        if (!force) {
            plugin.getVoidGUI().scheduleVoidOpen();
        }
    }

    /**
     * Pelne czyszczenie (/void fullclean lub krytyczne TPS).
     * Uzywa high-clean + fullclean-blacklist, nie otwiera voida.
     */
    public void fullCleanup() {
        List<EntityRule> rules     = config.getHighCleanRules();
        List<EntityRule> blacklist = config.getFullcleanBlacklistRules();

        List<Entity> toRemove = collectEntities(rules, blacklist);
        if (toRemove.isEmpty()) return;

        sendItemsToVoid(toRemove);

        for (Entity e : toRemove) e.remove();

        Bukkit.broadcastMessage(config.getMessage("cleanup-complete"));
    }

    public int getTimeUntilCleanup() {
        return timeUntilCleanup;
    }

    public void cleanup() {
        if (cleanupTask   != null) cleanupTask.cancel();
        if (warningTask   != null) warningTask.cancel();
        if (tpsMonitorTask != null) tpsMonitorTask.cancel();
    }

    // -----------------------------------------------------------------------
    // Logika filtrowania encji
    // -----------------------------------------------------------------------

    /**
     * Zbiera encje ze wszystkich swiatow ktore pasuja do regul
     * i nie sa na czarnej liscie ani chronione globalnie.
     */
    private List<Entity> collectEntities(List<EntityRule> rules, List<EntityRule> blacklist) {
        List<Entity> result = new ArrayList<>();
        for (var world : Bukkit.getWorlds()) {
            for (var entity : world.getEntities()) {
                if (shouldClean(entity, rules, blacklist)) {
                    result.add(entity);
                }
            }
        }
        return result;
    }

    private boolean shouldClean(Entity entity, List<EntityRule> rules, List<EntityRule> blacklist) {
        // Gracze nigdy nie sa czyszczeni
        if (entity instanceof Player) return false;

        // Globalna ochrona oswojonych zwierzat
        if (config.shouldProtectTamed()
                && entity instanceof Tameable t
                && t.isTamed()) return false;

        // Globalna ochrona encji z nametag
        if (config.shouldProtectNamed()
                && entity.getCustomName() != null) return false;

        // Czarna lista - jesli jakakolwiek regula pasuje, encja jest chroniona
        for (EntityRule rule : blacklist) {
            if (rule.matches(entity)) return false;
        }

        // Lista czyszczenia - encja jest usuwana jesli jakakolwiek regula pasuje
        for (EntityRule rule : rules) {
            if (rule.matches(entity)) return true;
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Przekazywanie itemow do voida
    // -----------------------------------------------------------------------

    private void sendItemsToVoid(List<Entity> entities) {
        List<Item> items = entities.stream()
                .filter(e -> e instanceof Item)
                .map(e -> (Item) e)
                .collect(Collectors.toList());

        if (!items.isEmpty()) {
            plugin.getVoidGUI().addItems(items);
        }
    }

    // -----------------------------------------------------------------------
    // World Cleanup (async)
    // -----------------------------------------------------------------------

    public void startWorldCleanup(World world, CommandSender requester) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CleanupScan scan = scanWorldAsync(world);
            // Teraz na main threadzie usuwaj entities i dodaj items do voidu
            Bukkit.getScheduler().runTask(plugin, () -> {
                List<Item> items = new ArrayList<>();
                for (java.util.UUID uuid : scan.entityUUIDs) {
                    Entity entity = world.getEntity(uuid);
                    if (entity != null && entity.isValid()) {
                        if (entity instanceof Item item) {
                            ItemStack stack = item.getItemStack();
                            if (stack != null && !stack.getType().isAir()) {
                                items.add(item);
                            }
                        }
                        entity.remove();
                    }
                }

                if (!items.isEmpty()) {
                    plugin.getVoidGUI().addItems(items);
                }
                requester.sendMessage(ChatColor.GREEN + "World cleanup complete! Removed " + scan.count + " entities/items.");
                Bukkit.broadcastMessage(ChatColor.AQUA + "World cleanup finished in " + world.getName() + " (" + scan.count + " entities removed)");
            });
        });
    }

    private CleanupScan scanWorldAsync(World world) {
        int count = 0;
        List<java.util.UUID> entityUUIDs = new ArrayList<>();
        Chunk[] chunks = world.getLoadedChunks();
        List<EntityRule> rules = config.getHighCleanRules();
        List<EntityRule> blacklist = config.getFullcleanBlacklistRules();

        for (Chunk chunk : chunks) {
            for (Entity entity : chunk.getEntities()) {
                if (shouldClean(entity, rules, blacklist)) {
                    entityUUIDs.add(entity.getUniqueId());
                    count++;
                }
            }
            // Opóźnienie aby nie obciążać serwera (async thread)
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return new CleanupScan(count, entityUUIDs);
    }

    private static class CleanupScan {
        int count;
        List<java.util.UUID> entityUUIDs;

        CleanupScan(int count, List<java.util.UUID> entityUUIDs) {
            this.count = count;
            this.entityUUIDs = entityUUIDs;
        }
    }

    // -----------------------------------------------------------------------
    // Monitor TPS
    // -----------------------------------------------------------------------

    private void startTpsMonitor() {
        tpsMonitorTask = new BukkitRunnable() {
            @Override public void run() {
                double tps = Bukkit.getTPS()[0];

                if (tps <= config.getCriticalTpsThreshold()) {
                    Bukkit.broadcastMessage(
                        config.getMessage("critical-tps-cleanup", "%tps%", String.format("%.2f", tps)));
                    fullCleanup();
                } else if (tps <= config.getLowTpsThreshold()) {
                    Bukkit.broadcastMessage(
                        config.getMessage("low-tps-cleanup", "%tps%", String.format("%.2f", tps)));
                    cleanupItems(true);
                }
            }
        }.runTaskTimer(plugin, config.getTpsCheckInterval() * 20L, config.getTpsCheckInterval() * 20L);
    }
}
