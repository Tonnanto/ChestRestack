package de.stamme.chestrestack.util;

import de.stamme.chestrestack.config.MinecraftLocaleConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class StringFormatter {

    /**
     * Transforms Strings to a user-friendly format. E.g.: DIAMOND_PICKAXE -> Diamond Pickaxe
     * @param string the input string
     * @return the formatted output string
     */
    public static String format(String string) {
        String[] words = string.split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String s = words[i];
            if (i != 0) {
                result.append(" ");
            }
            result.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * returns a formatted description of an ItemStack
     * format: 	<amount> <MaterialName>
     * @param itemStack the input item stack object to create a string for
     * @return the formatted output string
     */
    public static String formatItemStack(ItemStack itemStack, boolean showAmount) {
        StringBuilder s = new StringBuilder();
        if (showAmount) {
            int amount = itemStack.getAmount();
            s.append(amount).append(" ");
        }
        String itemName = MinecraftLocaleConfig.getMinecraftName(itemStack.getType().name(), "item.minecraft.");
        s.append(itemName);
        return s.toString();
    }

    public static String formatMaterialName(Material material) {
        StringBuilder s = new StringBuilder();
        String itemName = MinecraftLocaleConfig.getMinecraftName(material.name(), "item.minecraft.");
        s.append(itemName);
        return s.toString();
    }
}
