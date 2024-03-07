package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class ItemPreferenceCommand extends ChestRestackCommand {

    private final PlayerPreferences.ItemPreference itemPreference;

    protected ItemPreferenceCommand(PlayerPreferences.ItemPreference itemPreference) {
        super(itemPreference.toString());
        this.itemPreference = itemPreference;
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player player) || !sender.hasPermission(getPermission())) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        // /chestrestack preferences <item-preference> ...
        List<String> possible = new ArrayList<>();

        if (preferences.getItemPreference(itemPreference)) {
            possible.add("false");
        } else {
            possible.add("true");
        }
        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }


    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (params.size() > 1 || !(sender instanceof Player player)) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        if (params.isEmpty()) {
            // Toggle item preference if no argument was given
            onItemPreferenceChanged(player, preferences, itemPreference, !preferences.getItemPreference(itemPreference));
        } else if (params.get(0).equalsIgnoreCase("true")) {
            onItemPreferenceChanged(player, preferences, itemPreference, true);
        } else if (params.get(0).equalsIgnoreCase("false")) {
            onItemPreferenceChanged(player, preferences, itemPreference, false);
        }
    }

    private void onItemPreferenceChanged(Player player, PlayerPreferences preferences, PlayerPreferences.ItemPreference itemPreference, boolean enable) {
        preferences.setItemPreference(itemPreference, enable);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, preferences.getItemPreferenceMessage(itemPreference));
    }
}
