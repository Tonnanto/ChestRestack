package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.PlayerPreferences;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class MoveFromHotbarCommand extends ChestRestackCommand {

    protected MoveFromHotbarCommand() {
        super("movefromhotbar");
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player player) || !sender.hasPermission(getPermission())) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        // chestrestack movefromhotbar ...
        List<String> possible = new ArrayList<>();

        if (preferences.isMoveFromHotbar()) {
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
            // Toggle movefromhotbar if no argument was given
            enableMoveFromHotbar(!preferences.isMoveFromHotbar(), player, preferences);
        } else if (params.get(0).equalsIgnoreCase("true")) {
            enableMoveFromHotbar(true, player, preferences);
        } else if (params.get(0).equalsIgnoreCase("false")) {
            enableMoveFromHotbar(false, player, preferences);
        }
    }

    private void enableMoveFromHotbar(boolean enable, Player player, PlayerPreferences preferences) {
        preferences.setMoveFromHotbar(enable);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, MessagesConfig.getMessage("commands.movefromhotbar." + (enable ? "enabled" : "disabled")));
    }
}
