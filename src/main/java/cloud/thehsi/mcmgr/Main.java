package cloud.thehsi.mcmgr;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager manager = Bukkit.getPluginManager();
        Menu menu = new Menu(this);
        manager.registerEvents(menu, this);
        PluginCommand cmd = getCommand("menu");
        if (cmd == null) throw new RuntimeException("Cannot get command /menu");
        cmd.setExecutor(menu);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
