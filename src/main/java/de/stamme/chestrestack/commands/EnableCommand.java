package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class EnableCommand extends ChestRestackCommand {

    protected EnableCommand() {
        super("enable");
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (!(sender instanceof Player player)) return;

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());
        preferences.setEnabled(true);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);

        ChestRestack.sendMessage(sender, preferences.getRestackingMessage());
    }
}
