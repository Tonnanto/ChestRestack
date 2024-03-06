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

public class MoveArrowsCommand extends ChestRestackCommand {

    protected MoveArrowsCommand() {
        super("arrows");
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player player) || !sender.hasPermission(getPermission())) {
            return;
        }

        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        // /chestrestack preferences arrows ...
        List<String> possible = new ArrayList<>();

        if (preferences.isMoveArrows()) {
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
            // Toggle movearrows if no argument was given
            enableMoveArrows(!preferences.isMoveArrows(), player, preferences);
        } else if (params.get(0).equalsIgnoreCase("true")) {
            enableMoveArrows(true, player, preferences);
        } else if (params.get(0).equalsIgnoreCase("false")) {
            enableMoveArrows(false, player, preferences);
        }
    }

    private void enableMoveArrows(boolean enable, Player player, PlayerPreferences preferences) {
        preferences.setMoveArrows(enable);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, preferences.getArrowsMessage());
    }
}
