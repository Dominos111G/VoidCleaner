package pl.szczerbal.voidcleaner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VoidGUI implements Listener {
    private final VoidCleaner plugin;
    private final Config config;
    private final Map<String, List<ItemStack>> voidsByWorld;
    private final Map<UUID, Integer> playerPages;
    private final Set<UUID> navigatingPlayers;
    private boolean isOpen;
    private boolean isScheduledToOpen;
    private BukkitTask openScheduleTask;
    private BukkitTask closeTask;
    private BukkitTask warningTask;
    private int timeUntilClose;

    private static final int SLOT_PREV = 45;
    private static final int SLOT_INFO = 49;
    private static final int SLOT_NEXT = 53;
    private static final int PAGE_SIZE = 45;

    public VoidGUI(VoidCleaner plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        this.voidsByWorld = new HashMap<>();
        this.playerPages = new HashMap<>();
        this.navigatingPlayers = new HashSet<>();
        this.isOpen = false;
        this.isScheduledToOpen = false;
        this.openScheduleTask = null;

        // Zainicjuj voidy dla wszystkich znanych światów
        for (String voidId : config.getAllVoidIds()) {
            voidsByWorld.put(voidId, new ArrayList<>());
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private List<ItemStack> getVoidItemsForWorld(String worldName) {
        String voidId = config.getVoidIdForWorld(worldName);
        return voidsByWorld.computeIfAbsent(voidId, k -> new ArrayList<>());
    }

    public void addItems(List<Item> items) {
        int maxStorage = config.getMaxVoidStorage();

        // Mapuj itemy po ich światach
        Map<String, List<ItemStack>> itemsByWorld = new HashMap<>();
        for (Item item : items) {
            ItemStack stack = item.getItemStack();
            if (stack == null || stack.getType().isAir()) continue;
            stack = stack.clone();

            String worldName = item.getWorld().getName();
            String voidId = config.getVoidIdForWorld(worldName);
            itemsByWorld.computeIfAbsent(voidId, k -> new ArrayList<>()).add(stack);
        }

        // Dodaj itemy do odpowiednich voidów
        for (Map.Entry<String, List<ItemStack>> entry : itemsByWorld.entrySet()) {
            String voidId = entry.getKey();
            List<ItemStack> voidItems = voidsByWorld.computeIfAbsent(voidId, k -> new ArrayList<>());

            for (ItemStack stack : entry.getValue()) {
                if (maxStorage > 0) {
                    int freeSpace = maxStorage - getItemCount(voidId);
                    if (freeSpace <= 0) break;
                    if (stack.getAmount() > freeSpace) {
                        stack.setAmount(freeSpace);
                    }
                }
                addItemStack(voidId, stack);
            }
        }

        refreshAllOpenInventories();
    }

    private void addItemStack(String voidId, ItemStack stack) {
        List<ItemStack> voidItems = voidsByWorld.get(voidId);
        if (voidItems == null) return;

        for (ItemStack existing : voidItems) {
            if (existing.getType() == stack.getType() &&
                existing.getAmount() < existing.getMaxStackSize()) {
                int canAdd = existing.getMaxStackSize() - existing.getAmount();
                int toAdd = Math.min(canAdd, stack.getAmount());
                existing.setAmount(existing.getAmount() + toAdd);
                stack.setAmount(stack.getAmount() - toAdd);

                if (stack.getAmount() == 0) return;
            }
        }

        while (stack.getAmount() > 0) {
            ItemStack portion = stack.clone();
            int maxSize = portion.getMaxStackSize();
            if (portion.getAmount() > maxSize) {
                portion.setAmount(maxSize);
                stack.setAmount(stack.getAmount() - maxSize);
            } else {
                stack.setAmount(0);
            }
            voidItems.add(portion);
        }
    }

    public void openVoid() {
        if (isOpen) return;
        isScheduledToOpen = false;

        isOpen = true;
        timeUntilClose = config.getVoidCloseDelay();

        Bukkit.broadcastMessage(config.getMessage("void-opening"));

        final int[] closeTimes = config.getCloseWarningTimes();
        warningTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeUntilClose--;
                for (int t : closeTimes) {
                    if (timeUntilClose == t) {
                        Bukkit.broadcastMessage(config.getMessage("void-closing", "%seconds%", String.valueOf(t)));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        closeTask = new BukkitRunnable() {
            @Override
            public void run() {
                closeVoid();
            }
        }.runTaskLater(plugin, config.getVoidCloseDelay() * 20L);
    }

    public void scheduleVoidOpen() {
        if (isOpen || isScheduledToOpen) return;
        isScheduledToOpen = true;

        int[] warningTimes = config.getOpenWarningTimes();
        if (warningTimes.length == 0) {
            openVoid();
            return;
        }

        int maxWait = 0;
        for (int t : warningTimes) if (t > maxWait) maxWait = t;

        final int[] countdown = {maxWait};
        openScheduleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isScheduledToOpen) {
                    this.cancel();
                    openScheduleTask = null;
                    return;
                }
                for (int t : warningTimes) {
                    if (countdown[0] == t) {
                        Bukkit.broadcastMessage(
                            config.getMessage("void-opening-soon", "%seconds%", String.valueOf(t)));
                    }
                }
                countdown[0]--;
                if (countdown[0] < 0) {
                    this.cancel();
                    openScheduleTask = null;
                    openVoid();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void openInventory(Player player) {
        if (!isOpen) {
            player.sendMessage(config.getMessage("void-not-open"));
            return;
        }

        String voidId = config.getVoidIdForWorld(player.getWorld().getName());
        int page = playerPages.getOrDefault(player.getUniqueId(), 0);
        Inventory inv = createInventory(voidId, page);
        player.openInventory(inv);
    }

    private Inventory createInventory(String voidId, int page) {
        List<ItemStack> voidItems = voidsByWorld.getOrDefault(voidId, new ArrayList<>());
        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, voidItems.size());

        String title = ChatColor.DARK_PURPLE + "[" + voidId + "] Void - Page " + (page + 1);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        for (int i = startIndex; i < endIndex; i++) {
            inv.setItem(i - startIndex, voidItems.get(i));
        }

        if (page > 0) {
            ItemStack prevPage = createNavigationItem(ChatColor.RED + "Previous Page", "PREV_PAGE");
            inv.setItem(SLOT_PREV, prevPage);
        }

        if (endIndex < voidItems.size()) {
            ItemStack nextPage = createNavigationItem(ChatColor.GREEN + "Next Page", "NEXT_PAGE");
            inv.setItem(SLOT_NEXT, nextPage);
        }

        ItemStack info = createNavigationItem(
            ChatColor.YELLOW + "Items: " + getItemCount(voidId), "INFO");
        inv.setItem(SLOT_INFO, info);

        return inv;
    }

    private ItemStack createNavigationItem(String name, String type) {
        Material material = switch (type) {
            case "PREV_PAGE" -> Material.ARROW;
            case "NEXT_PAGE" -> Material.ARROW;
            case "INFO"      -> Material.NETHER_STAR;
            default -> Material.GRAY_STAINED_GLASS_PANE;
        };

        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Void")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        String voidId = config.getVoidIdForWorld(player.getWorld().getName());
        List<ItemStack> voidItems = voidsByWorld.getOrDefault(voidId, new ArrayList<>());

        if (slot == SLOT_PREV) {
            if (currentPage > 0) {
                playerPages.put(player.getUniqueId(), currentPage - 1);
                navigatingPlayers.add(player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> openInventory(player));
            }
            return;
        }

        if (slot == SLOT_NEXT) {
            int maxPage = voidItems.isEmpty() ? 0 : (voidItems.size() - 1) / PAGE_SIZE;
            if (currentPage < maxPage) {
                playerPages.put(player.getUniqueId(), currentPage + 1);
                navigatingPlayers.add(player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> openInventory(player));
            }
            return;
        }

        if (slot == SLOT_INFO) return;

        if (slot >= 0 && slot < PAGE_SIZE) {
            int itemIndex = currentPage * PAGE_SIZE + slot;
            if (itemIndex < voidItems.size()) {
                ItemStack item = voidItems.get(itemIndex);

                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "Your inventory is full!");
                    return;
                }

                voidItems.remove(itemIndex);
                player.getInventory().addItem(item);
                navigatingPlayers.add(player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    openInventory(player);
                    refreshAllOpenInventories();
                });
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().contains("Void")) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        if (navigatingPlayers.remove(uuid)) {
            return;
        }
        playerPages.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playerPages.remove(uuid);
        navigatingPlayers.remove(uuid);
    }

    public void closeVoid() {
        isOpen = false;
        isScheduledToOpen = false;

        if (openScheduleTask != null) {
            openScheduleTask.cancel();
            openScheduleTask = null;
        }
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
        if (closeTask != null) {
            closeTask.cancel();
            closeTask = null;
        }

        for (List<ItemStack> items : voidsByWorld.values()) {
            items.clear();
        }
        Bukkit.broadcastMessage(config.getMessage("void-closed"));

        List<Player> toClose = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().contains("Void")) {
                toClose.add(player);
            }
        }
        if (!toClose.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player player : toClose) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.RED + "The void has been closed!");
                }
            });
        }

        playerPages.clear();
        navigatingPlayers.clear();
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getItemCount(String voidId) {
        List<ItemStack> voidItems = voidsByWorld.getOrDefault(voidId, new ArrayList<>());
        return voidItems.stream().mapToInt(ItemStack::getAmount).sum();
    }

    private void refreshAllOpenInventories() {
        if (!isOpen) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().contains("Void")) {
                String voidId = config.getVoidIdForWorld(player.getWorld().getName());
                int page = playerPages.getOrDefault(player.getUniqueId(), 0);
                List<ItemStack> voidItems = voidsByWorld.getOrDefault(voidId, new ArrayList<>());
                int maxPage = voidItems.isEmpty() ? 0 : (voidItems.size() - 1) / PAGE_SIZE;
                if (page > maxPage) {
                    page = maxPage;
                    playerPages.put(player.getUniqueId(), page);
                }
                player.openInventory(createInventory(voidId, page));
            }
        }
    }
}
