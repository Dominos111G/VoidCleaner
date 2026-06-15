package pl.szczerbal.voidcleaner;

import org.bukkit.plugin.java.JavaPlugin;

public final class VoidCleaner extends JavaPlugin {

    private static VoidCleaner instance;
    private ItemCleaner itemCleaner;
    private VoidGUI voidGUI;
    private Config config;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        config = new Config(this);

        // VoidGUI musi byc utworzone PRZED ItemCleaner, poniewaz ItemCleaner
        // od razu startuje scheduler ktory moze wywolac plugin.getVoidGUI()
        voidGUI = new VoidGUI(this);
        itemCleaner = new ItemCleaner(this);
        
        // Jedna instancja obsługuje i executor i tab-completer
        VoidCommand voidCommand = new VoidCommand(this);
        getCommand("void").setExecutor(voidCommand);
        getCommand("void").setTabCompleter(voidCommand);
        
        getLogger().info("VoidCleaner has been enabled!");
    }

    @Override
    public void onDisable() {
        if (itemCleaner != null) {
            itemCleaner.cleanup();
        }
        getLogger().info("VoidCleaner has been disabled!");
    }

    public static VoidCleaner getInstance() {
        return instance;
    }

    public ItemCleaner getItemCleaner() {
        return itemCleaner;
    }

    public VoidGUI getVoidGUI() {
        return voidGUI;
    }

    public Config getPluginConfig() {
        return config;
    }
}
