package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class MoveToolsCommand extends ChestRestackCommand {

    protected MoveToolsCommand() {
        super("tools");
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player player) || !sender.hasPermission(getPermission())) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        // chestrestack movetools ...
        List<String> possible = new ArrayList<>();

        if (preferences.isMoveTools()) {
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
            // Toggle movetools if no argument was given
            enableMoveTools(!preferences.isMoveTools(), player, preferences);
        } else if (params.get(0).equalsIgnoreCase("true")) {
            enableMoveTools(true, player, preferences);
        } else if (params.get(0).equalsIgnoreCase("false")) {
            enableMoveTools(false, player, preferences);
        }
    }

    private void enableMoveTools(boolean enable, Player player, PlayerPreferences preferences) {
        preferences.setMoveTools(enable);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, MessagesConfig.getMessage("commands.tools." + (enable ? "enabled" : "disabled")));
    }
}
