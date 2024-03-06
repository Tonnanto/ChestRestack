package de.stamme.chestrestack.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import de.stamme.chestrestack.model.PlayerPreferences;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.util.*;

public class PreferencesCommand extends ChestRestackCommand {
    @Unmodifiable
    private static final List<ChestRestackCommand> COMMANDS = ImmutableList.of(
            new ClickModeCommand(),
            new SortingCommand(),
            new MoveFromHotbarCommand(),
            new MoveToolsCommand(),
            new MoveArmorCommand(),
            new MoveArrowsCommand(),
            new ResetPreferencesCommand()
    );

    @NotNull
    @Unmodifiable
    private final Map<String, ChestRestackCommand> commands;

    protected PreferencesCommand() {
        super("preferences", "prefs");

        final ImmutableMap.Builder<String, ChestRestackCommand> commands = ImmutableMap.builder();

        for (final ChestRestackCommand command : COMMANDS) {
            command.getLabels().forEach(label -> commands.put(label, command));
        }

        this.commands = commands.build();
    }

    @Override
    public void complete(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params, @NotNull List<String> suggestions) {
        if (!(sender instanceof Player) || !sender.hasPermission(getPermission())) {
            return;
        }

        if (params.size() > 1) {
            final String search = params.get(0).toLowerCase(Locale.ROOT);
            final ChestRestackCommand target = commands.get(search);
            if (target != null) {
                target.complete(plugin, sender, alias, params.subList(1, params.size()), suggestions);
            }
            return;
        }

        // /chestrestack preferences ...
        List<String> possible = new ArrayList<>();

        if (Config.getSortingEnabledGlobal()) {
            possible.add("sorting");
        }

        possible.add("armor");
        possible.add("arrows");
        possible.add("tools");
        possible.add("hotbar");
        possible.add("clickmode");
        possible.add("reset");

        suggestByParameter(possible.stream(), suggestions, params.get(params.size() - 1));
    }

    @Override
    public void evaluate(@NotNull ChestRestack plugin, @NotNull CommandSender sender, @NotNull String alias, @NotNull @Unmodifiable List<String> params) {
        if (!(sender instanceof Player player)) return;

        if (params.isEmpty()) {
            onShowPreferences(plugin, player);
            return;
        }

        final String search = params.get(0).toLowerCase(Locale.ROOT);
        final ChestRestackCommand target = commands.get(search);

        if (target == null) {
            ChestRestack.sendMessage(sender, MessageFormat.format(MessagesConfig.getMessage("generic.unknown-command"), "preferences " + search));
            return;
        }

        final String permission = target.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            ChestRestack.sendMessage(sender, MessagesConfig.getMessage("generic.no-permission"));
            return;
        }

        target.evaluate(plugin, sender, search, params.subList(1, params.size()));
    }

    private void onShowPreferences(ChestRestack plugin, Player player) {
        PlayerPreferences preferences = plugin.getPlayerPreference(player.getUniqueId());

        ChestRestack.sendRawMessage(player, preferences.getMessage());
    }
}
