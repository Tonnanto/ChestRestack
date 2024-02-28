package de.stamme.chestrestack.util;

import de.stamme.chestrestack.ChestRestack;
import org.bstats.bukkit.Metrics;

public class MetricsService {
    public static final int pluginId = 420; // TODO: Adjust ID

    public static void setUpMetrics() {
        Metrics metrics = new Metrics(ChestRestack.getPlugin(), pluginId);


        // TODO: Add custom charts
    }
}
