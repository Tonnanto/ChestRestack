package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class HelpCommand extends ChestRestackCommand {

    protected HelpCommand() {
        super("help");
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        PlayerPreferences preferences;
        if (sender instanceof Player) {
            preferences = plugin.getPlayerPreference(((Player) sender).getUniqueId());
        } else {
            preferences = Config.getDefaultPlayerPreferences();
        }

        StringBuilder sb = new StringBuilder();
        // Title - Header
        sb.append("\n&bChestRestack &8- &7Help Menu");
        // General description
        sb.append(String.format("\n%s click on a chest to move items from your inventory to the chest that were already present in the chest.", preferences.getClickMode().toString()));
        // Status (enabled / disabled) (sorting enabled / disabled)
        sb.append("\n\n");
        sb.append(statusString(preferences));
        // Commands help
        sb.append("\n");
        sb.append("\n&b/chestrestack &9<enable | disable>");
        sb.append("\n  &7Enable or disable the plugin for you");
        sb.append("\n&b/chestrestack &fsorting &9<true | false>");
        sb.append("\n  &7Enable or disable the sorting of chests after restacking");
        // TODO: add commands
        // TODO: move strings to messages.yml files


        ChestRestack.sendRawMessage(sender, sb.toString());
    }

    private String statusString(PlayerPreferences preferences) {
        String s = "&fRestacking ";
        if (!preferences.isEnabled()) {
            s += "&cdisbled &8- &7to enable use &b/chestrestack enable";
        } else {
            s += "&aenabled &8- &r";
            if (!Config.getSortingEnabledGlobal()) {
                s += "Sorting &cdisabled &fon this server";
            } else if (!preferences.isSortingEnabled()) {
                s += "Sorting &coff &8- &7to enable use &b/chestrestack sorting enable";
            } else {
                s += "Sorting &aon";
            }
        }

        return s;
    }
}
