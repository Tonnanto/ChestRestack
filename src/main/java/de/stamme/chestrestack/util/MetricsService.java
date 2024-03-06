package de.stamme.chestrestack.util;

import de.stamme.chestrestack.ChestRestack;
import org.bstats.bukkit.Metrics;

public class MetricsService {
    public static final int pluginId = 21237;

    public static void setUpMetrics() {
        Metrics metrics = new Metrics(ChestRestack.getPlugin(), pluginId);

        // TODO: Add custom charts

        // Locale
        // Total Restacks in past 7 days
        // Enabled vs Disabled
        // Sorting Enabled vs Disabled
        // Click Mode
        // Hotbar
        // Tools & Weapons
        // Armor
        // Arrows & Rockets
    }
}
