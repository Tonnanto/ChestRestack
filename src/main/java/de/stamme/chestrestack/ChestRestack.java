package de.stamme.chestrestack;

import de.stamme.chestrestack.util.BukkitVersion;
import de.stamme.chestrestack.commands.ChestRestackCommandRouter;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.config.MinecraftLocaleConfig;
import de.stamme.chestrestack.listeners.ClickBlockListener;
import de.stamme.chestrestack.listeners.PlayerJoinListener;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.util.MetricsService;
import de.stamme.chestrestack.util.UpdateChecker;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionDefault;
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

        // run after reload is complete
        getServer().getScheduler().runTask(this, () -> {

            // reload PlayerData for online players
            reloadPlayerPreferences();

            // Programmatically set the default permission value cause Bukkit doesn't handle plugin.yml properly for Load order STARTUP plugins
            org.bukkit.permissions.Permission perm = getServer().getPluginManager().getPermission("chestrestack.admin.update");

            if (perm == null) {
                perm = new org.bukkit.permissions.Permission("chestrestack.admin.update");
                perm.setDefault(PermissionDefault.OP);
                plugin.getServer().getPluginManager().addPermission(perm);
            }

            perm.setDescription("Allows a user or the console to check for ChestRestack updates");

            UpdateChecker.getInstance();
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(getPlugin());
    }

    /**
     * Reload the plugin configuration.
     */
    public void reload() {
        Config.reload();
        MessagesConfig.register(Config.getLocale());
        MinecraftLocaleConfig.register();
    }

    /**
     * Register the plugin configuration.
     */
    private void registerConfigs() {
        Config.register();
        MessagesConfig.register(Config.getLocale());
        MinecraftLocaleConfig.register();
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
     * Reload the player preferences.
     */
    private void reloadPlayerPreferences() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (!PlayerPreferences.loadPlayerData(player)) {
                ChestRestack.getPlugin().getPlayerPreferences().put(player.getUniqueId(), Config.getDefaultPlayerPreferences());
            }
        }
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

    /**
     * Retrieve the Bukkit version.
     *
     * @return BukkitVersion
     */
    public static BukkitVersion getBukkitVersion() {
        String serverVersionString = ChestRestack.getPlugin().getServer().getBukkitVersion();
        if (serverVersionString.contains("1.16"))
            return BukkitVersion.v1_16;

        if (serverVersionString.contains("1.17"))
            return BukkitVersion.v1_17;

        if (serverVersionString.contains("1.18"))
            return BukkitVersion.v1_18;

        if (serverVersionString.contains("1.19"))
            return BukkitVersion.v1_19;

        return BukkitVersion.v1_20;
    }
}
