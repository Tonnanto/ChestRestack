package de.stamme.chestrestack;

import de.stamme.chestrestack.commands.ChestRestackCommandRouter;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.listeners.ClickBlockListener;
import de.stamme.chestrestack.listeners.PlayerJoinListener;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class ChestRestack extends JavaPlugin {
    private static ChestRestack plugin;
    private static String userdataPath;
    private static final int spigotMCID = 87972; // TODO Adjust!
    private final HashMap<UUID, PlayerPreferences> playerPreferences = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        userdataPath = this.getDataFolder() + "/userdata";

        registerConfigs();

        // Loading commands and listeners
        registerCommands();
        registerListeners();

        // create userdata directory
        File userFile = new File(userdataPath);
        if (!userFile.exists()) {
            if (!userFile.mkdir()) {
                ChestRestack.log(Level.SEVERE, String.format("Failed to create directory %s", userFile.getPath()));
            }
        }

        MetricsService.setUpMetrics();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(getPlugin());
    }

    /**
     * Reload the plugin configuration and quest generation files.
     */
    public void reload() {
        Config.reload();
        MessagesConfig.register(Config.getLocale());
//        MinecraftLocaleConfig.register(); // TODO: Use MinecraftLocaleConfig?
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
        pluginManager.registerEvents(new PlayerJoinListener(), this);
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

    /**
     * Retrieve the mqp of player preferences.
     *
     * @return Map
     */
    @NotNull
    public Map<UUID, PlayerPreferences> getPlayerPreferences() {
        return playerPreferences;
    }

    /**
     * Retrieve a player's preferences.
     *
     * @param uuid The player's UUID.
     * @return PlayerPreferences
     */
    @NotNull
    public PlayerPreferences getPlayerPreference(UUID uuid) {
        PlayerPreferences preferences = playerPreferences.get(uuid);
        if (preferences == null) preferences = Config.getDefaultPlayerPreferences();
        return preferences;
    }

    /**
     * Retrieve the userdata path.
     *
     * @return String
     */
    public static String getUserdataPath() {
        return userdataPath;
    }
}
