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

import static de.stamme.chestrestack.model.PlayerPreferences.ClickMode.*;

public class ClickModeCommand extends ChestRestackCommand {

    protected ClickModeCommand() {
        super("clickmode");
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (params.size() > 1 || !(sender instanceof Player) || !sender.hasPermission(getPermission())) {
            return;
        }

        // /chestrestack preferences clickmode ...
        List<String> possible = new ArrayList<>();

        for (PlayerPreferences.ClickMode mode : PlayerPreferences.ClickMode.values()) {
            possible.add(mode.toString());
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
            // Toggle click mode between shift-left and shift-right if no argument was given
            setClickMode(preferences.getClickMode() == SHIFT_LEFT ? SHIFT_RIGHT : SHIFT_LEFT, player, preferences);
        } else if (params.get(0).equalsIgnoreCase(SHIFT_LEFT.toString())) {
            setClickMode(SHIFT_LEFT, player, preferences);
        } else if (params.get(0).equalsIgnoreCase(SHIFT_RIGHT.toString())) {
            setClickMode(SHIFT_RIGHT, player, preferences);
        }
    }

    private void setClickMode(PlayerPreferences.ClickMode mode, Player player, PlayerPreferences preferences) {
        preferences.setClickMode(mode);
        PlayerPreferences.savePreferencesForPlayer(preferences, player);
        ChestRestack.sendMessage(player, preferences.getClickmodeMessage());
    }
}
