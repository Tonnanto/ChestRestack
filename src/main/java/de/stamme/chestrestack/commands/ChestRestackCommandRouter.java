package de.stamme.chestrestack.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

public class ChestRestackCommandRouter implements CommandExecutor, TabCompleter {
    @Unmodifiable
    private static final List<ChestRestackCommand> COMMANDS = ImmutableList.of(
            new HelpCommand(),
            new EnableCommand(),
            new DisableCommand(),
            new ReloadCommand(),
//            new SortingCommand(),
//            new ClickModeCommand(),
//            new MoveFromHotbarCommand(),
//            new MoveToolsCommand(),
//            new MoveArmorCommand(),
//            new MoveArrowsCommand(),
            new PreferencesCommand()
    );

    @NotNull
    private final ChestRestack plugin;

    @NotNull
    @Unmodifiable
    private final Map<String, ChestRestackCommand> commands;


    public ChestRestackCommandRouter(@NotNull final ChestRestack plugin) {
        this.plugin = plugin;

        final ImmutableMap.Builder<String, ChestRestackCommand> commands = ImmutableMap.builder();

        for (final ChestRestackCommand command : COMMANDS) {
            command.getLabels().forEach(label -> commands.put(label, command));
        }

        this.commands = commands.build();
    }


    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        if (args.length == 0) {
            // fallback to "/chestrestack help" if no arguments were given
            final ChestRestackCommand fallback = commands.get("help");
            if (fallback != null) {
                fallback.evaluate(plugin, sender, "", Collections.emptyList());
            }

            return true;
        }

        final String search = args[0].toLowerCase(Locale.ROOT);
        final ChestRestackCommand target = commands.get(search);

        if (target == null) {
            ChestRestack.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.unknown-command"), search));
            return true;
        }

        final String permission = target.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            ChestRestack.sendMessage(sender, MessagesConfig.getMessage("generic.no-permission"));
            return true;
        }

        target.evaluate(plugin, sender, search, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender,
                                      @NotNull final Command command, @NotNull final String alias, @NotNull final String[] args) {
        final List<String> suggestions = new ArrayList<>();

        if (args.length > 1) {
            final ChestRestackCommand target = this.commands.get(args[0].toLowerCase(Locale.ROOT));

            if (target != null) {
                target.complete(plugin, sender, args[0].toLowerCase(Locale.ROOT), Arrays.asList(Arrays.copyOfRange(args, 1, args.length)), suggestions);
            }

            return suggestions;
        }

        PlayerPreferences preferences;
        if (sender instanceof Player) {
            preferences = plugin.getPlayerPreference(((Player) sender).getUniqueId());
        } else {
            preferences = Config.getDefaultPlayerPreferences();
        }

        final Stream<String> targets = ChestRestackCommand
                .filterByPermission(sender, commands.values().stream().filter((ChestRestackCommand cmd) -> {
                    if (cmd instanceof EnableCommand && preferences.isEnabled()) return false;
                    if (cmd instanceof DisableCommand && !preferences.isEnabled()) return false;
                    if (cmd instanceof SortingCommand && !Config.getSortingEnabledGlobal()) return false;
                    return true;
                })).map(ChestRestackCommand::getLabels)
                .flatMap(Collection::stream);
        ChestRestackCommand.suggestByParameter(targets, suggestions, args.length == 0 ? null : args[0]);

        return suggestions;
    }
}
