package pl.szczerbal.voidcleaner.version;

import org.bukkit.inventory.ItemStack;

/**
 * Abstraction for version-specific operations.
 * Each Minecraft version may have different APIs, so this interface
 * allows us to implement version-specific logic.
 */
public interface VersionHandler {

    /**
     * Get the Minecraft version this handler supports
     */
    String getMinecraftVersion();

    /**
     * Get the maximum stack size for an item type
     */
    int getMaxStackSize(ItemStack stack);

    /**
     * Check if this handler supports the current server version
     */
    boolean isSupported();

    /**
     * Get version info for logging
     */
    default String getVersionInfo() {
        return "VoidCleaner on Minecraft " + getMinecraftVersion();
    }
}
