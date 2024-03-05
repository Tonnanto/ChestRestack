package de.stamme.chestrestack.util;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;

    @Nullable
    private String newVersion;
    private final String currentVersion;

    private static UpdateChecker instance;

    public static UpdateChecker getInstance() {
        if (instance == null) {
            instance = new UpdateChecker(ChestRestack.getPlugin(), ChestRestack.getSpigotMCID());
        }
        return instance;
    }

    private UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.currentVersion = plugin.getDescription().getVersion();
        register();
    }

    public void register() {
        if (!Config.checkForUpdates()) return;

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            ChestRestack.log("Checking for Updates ... ");
            getVersion(version -> {
                String oldVersion = plugin.getDescription().getVersion();
                if (oldVersion.equalsIgnoreCase(version)) {
                    ChestRestack.log("No new version available.");
                } else {
                    notifyUser(plugin.getServer().getConsoleSender());
                    for (CommandSender player : plugin.getServer().getOnlinePlayers()) {
                        if (player.hasPermission("chestrestack.admin.update")) {
                            notifyUser(player);
                        }
                    }
                }
            });

        }, 0, 432000);
    }

    /**
     * Pulls the most recent version of ChestRestack from SpigotMC
     *
     * @param consumer the consumer to accept the pulled version string
     */
    private void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    String version = scanner.next();
                    newVersion = version;
                    consumer.accept(version);
                }
            } catch (IOException exception) {
                ChestRestack.log("Cannot look for updates: " + exception.getMessage());
            }
        });
    }

    /**
     * Notifies a CommandSender of the new version
     *
     * @param user CommandSender
     */
    public void notifyUser(CommandSender user) {
        if (!Config.checkForUpdates()) return;
        if (newVersion == null || newVersion.equals(currentVersion)) return;

        user.sendMessage(ChatColor.GREEN + String.format("Version %s of ChestRestack is now available:", newVersion));
        user.sendMessage(ChatColor.DARK_GREEN + plugin.getDescription().getWebsite());
        user.sendMessage(ChatColor.GREEN + String.format("Your version: %s", currentVersion));
    }
}
