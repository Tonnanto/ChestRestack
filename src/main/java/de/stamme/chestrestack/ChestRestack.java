package de.stamme.chestrestack;

import de.stamme.chestrestack.commands.ChestRestackCommandRouter;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.listeners.ClickBlockListener;
import de.stamme.chestrestack.util.MetricsService;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import de.themoep.minedown.MineDown;

import java.util.logging.Level;

public final class ChestRestack extends JavaPlugin {
    private static ChestRestack plugin;
    private static final int spigotMCID = 420; // TODO Adjust!
    private static final int spigotMCID = 87972; // TODO Adjust!

    @Override
    public void onEnable() {
        plugin = this;

        registerConfigs();

        // Loading commands and listeners
        registerCommands();
        registerListeners();

        MetricsService.setUpMetrics();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(getPlugin());
    }

    /**
     * Register the plugin configuration.
     */
    private void registerConfigs() {
        Config.register();
        MessagesConfig.register(Config.getLocale());
    }

    /**
     * Register the plugin commands.
     */
    private void registerCommands() {
        final PluginCommand pluginCommand = getCommand("chestrestack");

        if (pluginCommand == null) {
            return;
        }

        final ChestRestackCommandRouter router = new ChestRestackCommandRouter(this);

        pluginCommand.setExecutor(router);
        pluginCommand.setTabCompleter(router);
    }

    /**
     * Register the plugin listeners.
     */
    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ClickBlockListener(), this);
    }

    /**
     * Log a message to console.
     *
     * @param message The message.
     */
    public static void log(String message) {
        log(Level.INFO, message);
    }

    /**
     * Log a message to console.
     *
     * @param level   The log level.
     * @param message The message.
     */
    public static void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    /**
     * Send an action bar message formatted with MineDown.
     *
     * @param player The player.
     * @param value  The message.
     */
    public static void sendActionMessage(Player player, String value) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                MineDown.parse(value)
        );
    }

    /**
     * Broadcast a message formatted with MineDown.
     *
     * @param value The message.
     */
    public static void broadcastMessage(String value) {
        getPlugin().getServer().getOnlinePlayers().forEach(player -> sendMessage(player, value));
    }

    /**
     * Send a message formatted with MineDown.
     *
     * @param sender The command sender.
     * @param value  The message.
     */
    public static void sendMessage(CommandSender sender, String value) {
        sender.spigot().sendMessage(
                MineDown.parse(MessagesConfig.getMessage("generic.prefix") + value)
        );
    }

    /**
     * Send a raw message formatted with MineDown.
     *
     * @param sender The command sender.
     * @param value  The message.
     */
    public static void sendRawMessage(CommandSender sender, String value) {
        sender.spigot().sendMessage(
                MineDown.parse(value)
        );
    }


    /**
     * Retrieve the plugin instance.
     *
     * @return ChestRestack
     */
    public static ChestRestack getPlugin() {
        return plugin;
    }


    /**
     * Retrieve the Spigot plugin ID.
     *
     * @return int
     */
    public static int getSpigotMCID() {
        return spigotMCID;
    }
}
