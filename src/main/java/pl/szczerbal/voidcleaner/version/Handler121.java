package pl.szczerbal.voidcleaner.version;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Handler for Paper Minecraft 1.21.0 - 1.21.11
 * Uses stable Paper API (mostly same as 1.20)
 */
public class Handler121 implements VersionHandler {

    @Override
    public String getMinecraftVersion() {
        return "1.21.x";
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return 0;
        }
        return stack.getMaxStackSize();
    }

    @Override
    public boolean isSupported() {
        String version = Bukkit.getVersion();
        return version.contains("1.21");
    }
}
