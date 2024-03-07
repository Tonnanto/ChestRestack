package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
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
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.header"));

        // General description
        sb.append(MessageFormat.format(MessagesConfig.getMessage("commands.help.info"), MessagesConfig.getMessage(preferences.getClickMode().messageKey())));

        // Status (enabled / disabled) (sorting enabled / disabled)
        sb.append("\n\n");
        sb.append(preferences.getRestackingMessage());

        // Commands help
        sb.append("\n");
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.enable-disable-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.enable-disable-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-sorting-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-sorting-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-clickmode-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-clickmode-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-reset-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-reset-info"));

        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-itemprefs-command"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-itemprefs-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-tools-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-armor-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-arrows-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-food-info"));
        sb.append("\n").append(MessagesConfig.getMessage("commands.help.preferences-potions-info"));

        if (sender.hasPermission("chestrestack.admin.reload")) {
            sb.append("\n").append(MessagesConfig.getMessage("commands.help.reload-command"));
            sb.append("\n").append(MessagesConfig.getMessage("commands.help.reload-info"));
        }

        sb.append("\n");

        ChestRestack.sendRawMessage(sender, sb.toString());
    }
}
