package de.stamme.chestrestack.listeners;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.util.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // load player preferences from file - if not successful, generate new PlayerPreferences and save them to file
        if (!PlayerPreferences.loadPlayerData(player)) {
            PlayerPreferences defaultPreferences = Config.getDefaultPlayerPreferences();
            PlayerPreferences.savePreferencesForPlayer(defaultPreferences, player);
        }

        // Notify player if a new version is available
        if (player.hasPermission("chestrestack.admin.update")) {
            ChestRestack.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(ChestRestack.getPlugin(),
                    () -> UpdateChecker.getInstance().notifyUser(player), 50L);
        }
    }
}
