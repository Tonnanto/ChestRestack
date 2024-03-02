package de.stamme.chestrestack.commands;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.stamme.chestrestack.ChestRestack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ChestRestackCommand {

    @NotNull
    private final String label;

    @NotNull
    private final Set<String> alias;

    protected ChestRestackCommand(@NotNull final String label, @NotNull final String... alias) {
        this.label = label;
        this.alias = Sets.newHashSet(alias);
    }

    @NotNull
    public static Stream<ChestRestackCommand> filterByPermission(@NotNull final CommandSender sender, @NotNull final Stream<ChestRestackCommand> commands) {
        return commands.filter(target -> target.getPermission() == null || sender.hasPermission(target.getPermission()));
    }

    public static void suggestByParameter(@NotNull final Stream<String> possible, @NotNull final List<String> suggestions, @Nullable final String parameter) {
        if (parameter == null) {
            possible.forEach(suggestions::add);
        } else {
            possible.filter(suggestion -> suggestion.toLowerCase(Locale.ROOT).startsWith(parameter.toLowerCase(Locale.ROOT))).forEach(suggestions::add);
        }
    }

    @NotNull
    @Unmodifiable
    public final Set<String> getAlias() {
        return ImmutableSet.copyOf(alias);
    }

    @NotNull
    @Unmodifiable
    public final Set<String> getLabels() {
        return ImmutableSet.<String>builder().add(label).addAll(alias).build();
    }

    public String getPermission() {
        return "chestrestack.use";
    }

    public abstract void evaluate(@NotNull final ChestRestack plugin,
                                  @NotNull final CommandSender sender, @NotNull final String alias,
                                  @NotNull @Unmodifiable final List<String> params);

    public void complete(@NotNull final ChestRestack plugin,
                         @NotNull final CommandSender sender, @NotNull final String alias,
                         @NotNull @Unmodifiable final List<String> params, @NotNull final List<String> suggestions) {

    }
}
