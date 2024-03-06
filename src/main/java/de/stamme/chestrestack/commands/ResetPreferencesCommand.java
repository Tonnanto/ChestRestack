package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.model.PlayerPreferences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ResetPreferencesCommand extends ChestRestackCommand {

    protected ResetPreferencesCommand() {
        super("reset");
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (params.size() > 1 || !(sender instanceof Player player)) {
            return;
        }

        PlayerPreferences.savePreferencesForPlayer(Config.getDefaultPlayerPreferences(), player);
        ChestRestack.sendMessage(player, MessagesConfig.getMessage("commands.reset"));
    }
}
