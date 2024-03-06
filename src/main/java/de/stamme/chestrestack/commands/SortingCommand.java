package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class SortingCommand extends ChestRestackCommand {

    protected SortingCommand() {
        super("sorting");
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player player) || !sender.hasPermission(getPermission()) || !Config.getSortingEnabledGlobal()) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        // /chestrestack preferences sorting ...
        List<String> possible = new ArrayList<>();

        if (preferences.isSortingEnabled()) {
            possible.add("disable");
        } else {
            possible.add("enable");
        }
        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (params.size() > 1 || !(sender instanceof Player player)) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        if (!Config.getSortingEnabledGlobal()) {
            ChestRestack.sendMessage(sender, MessagesConfig.getMessage("commands.sorting.disabled-globally"));
            return;
        }

        if (params.isEmpty()) {
            // Toggle sorting if no argument was given
            enableSorting(!preferences.isSortingEnabled(), player, preferences);
        } else if (params.get(0).equalsIgnoreCase("enable")) {
            enableSorting(true, player, preferences);
        } else if (params.get(0).equalsIgnoreCase("disable")) {
            enableSorting(false, player, preferences);
        }
    }

    private void enableSorting(boolean enable, Player player, PlayerPreferences preferences) {
        preferences.setSortingEnabled(enable);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, preferences.getSortingMessage());
    }
}
