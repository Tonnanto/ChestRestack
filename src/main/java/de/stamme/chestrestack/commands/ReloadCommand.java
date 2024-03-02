package de.stamme.chestrestack.commands;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ReloadCommand extends ChestRestackCommand {
    protected ReloadCommand() {
        super("reload");
    }

    @Override
    public final @NotNull String getPermission() {
        return "chestrestack.admin.reload";
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        plugin.reload();
        ChestRestack.sendMessage(sender, MessagesConfig.getMessage("commands.reload.success"));
    }
}
