package de.stamme.chestrestack.util;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.ServerInfo;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.model.PlayerPreferences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class MetricsService {
    public static final int pluginId = 21237;

    public static void setUpMetrics() {
        Metrics metrics = new Metrics(ChestRestack.getPlugin(), pluginId);

        // restacks in past 7 days line chart
        metrics.addCustomChart(new SingleLineChart("restack_count", () -> ServerInfo.getInstance().getRestackCount()));

        // items restackd in past 7 days line chart
        metrics.addCustomChart(new SingleLineChart("items_restacked_count", () -> ServerInfo.getInstance().getRestackedItemsCount()));

        // locale pie chart
        metrics.addCustomChart(new SimplePie("locale", Config::getLocale));

        // sorting enabled globally pie chart
        metrics.addCustomChart(new SimplePie("sorting_enabled_global", () -> Config.getSortingEnabledGlobal() ? "enabled" : "disabled"));

        // sound enabled pie chart
        metrics.addCustomChart(new SimplePie("sound_enabled", () -> Config.getRestackSoundEnabled() ? "enabled" : "disabled"));

        // enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("restacking_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithRestacking(true));
                valueMap.put("disabled", getPlayersWithRestacking(false));
                return valueMap;
            }

            private int getPlayersWithRestacking(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isEnabled() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));

        // sorting enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("sorting_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithSorting(true));
                valueMap.put("disabled", getPlayersWithSorting(false));
                return valueMap;
            }

            private int getPlayersWithSorting(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isSortingEnabled() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));

        // click mode advanced pie chart
        metrics.addCustomChart(new AdvancedPie("click_mode", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                for (PlayerPreferences.ClickMode clickMode : PlayerPreferences.ClickMode.values()) {
                    valueMap.put(clickMode.toString(), getPlayersWithClickmode(clickMode));
                }
                return valueMap;
            }

            private int getPlayersWithClickmode(PlayerPreferences.ClickMode clickmode) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).getClickMode() == clickmode) {
                        counter++;
                    }
                }
                return counter;
            }
        }));

        // hotbar enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("hotbar_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithHotbar(true));
                valueMap.put("disabled", getPlayersWithHotbar(false));
                return valueMap;
            }

            private int getPlayersWithHotbar(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isMoveFromHotbar() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));


        // tools & weapons enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("tools_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithTools(true));
                valueMap.put("disabled", getPlayersWithTools(false));
                return valueMap;
            }

            private int getPlayersWithTools(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isMoveTools() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));

        // armor enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("armor_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithArmor(true));
                valueMap.put("disabled", getPlayersWithArmor(false));
                return valueMap;
            }

            private int getPlayersWithArmor(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isMoveArmor() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));

        // arrows & rockets enabled advanced pie chart
        metrics.addCustomChart(new AdvancedPie("arrows_enabled", new Callable<>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("enabled", getPlayersWithArrows(true));
                valueMap.put("disabled", getPlayersWithArrows(false));
                return valueMap;
            }

            private int getPlayersWithArrows(boolean enabled) {
                int counter = 0;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (ChestRestack.getPlugin().getPlayerPreference(player.getUniqueId()).isMoveArrows() == enabled) {
                        counter++;
                    }
                }
                return counter;
            }
        }));
    }
}
