package de.stamme.chestrestack.model;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.config.Config;
import de.stamme.chestrestack.config.MessagesConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class PlayerPreferences implements Serializable {
    @Serial
    private static final long serialVersionUID = 420L;

    public enum ClickMode {
        SHIFT_LEFT, SHIFT_RIGHT;

        @Override
        public String toString() {
            return name().replace("_", "-").toLowerCase();
        }

        public String messageKey() {
            return String.format("preferences.clickmode-%s", this);
        }
    }

    private boolean enabled;
    private ClickMode clickMode;
    private boolean sortingEnabled;
    private boolean moveFromHotbar;
    private boolean moveTools;
    private boolean moveArmor;
    private boolean moveArrows;

    public PlayerPreferences(boolean enabled, ClickMode clickMode, boolean sortingEnabled, boolean moveFromHotbar, boolean moveTools, boolean moveArmor, boolean moveArrows) {
        this.enabled = enabled;
        this.clickMode = clickMode;
        this.sortingEnabled = sortingEnabled;
        this.moveFromHotbar = moveFromHotbar;
        this.moveTools = moveTools;
        this.moveArmor = moveArmor;
        this.moveArrows = moveArrows;
    }

    /**
     * Saves the PlayerPreferences to a dedicated file
     *
     * @param filePath the path to save the data to
     * @return whether the operation was successful
     */
    private boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(Paths.get(filePath))));
            out.writeObject(this);
            out.close();

            return true;
        } catch (IOException e) {
            ChestRestack.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    public static PlayerPreferences loadData(String filePath) {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(Files.newInputStream(Paths.get(filePath))));
            PlayerPreferences preferences = (PlayerPreferences) in.readObject();
            in.close();
            return preferences;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }

    /**
     * saves a player's preferences to a dedicated file
     *
     * @param preferences the preferences to save
     * @param player      the player to save it for
     * @return whether the operation was successful
     */
    public static boolean savePreferencesForPlayer(PlayerPreferences preferences, Player player) {
        ChestRestack.getPlugin().getPlayerPreferences().put(player.getUniqueId(), preferences);
        return preferences.saveData(filePathForUUID(player.getUniqueId()));
    }

    /**
     * loads a player's preferences from file when available. returns whether the operation was successful.
     *
     * @param player the player to load the data from
     * @return whether the operation was successful
     */
    public static boolean loadPlayerData(Player player) {

        String filepath = filePathForUUID(player.getUniqueId());
        if (!(new File(filepath)).exists()) {
            return false;
        }

        PlayerPreferences preferences = PlayerPreferences.loadData(filepath);

        if (preferences != null) {
            ChestRestack.getPlugin().getPlayerPreferences().put(player.getUniqueId(), preferences);
            ChestRestack.log(Level.INFO, "PlayerPreferences loaded: " + player.getName()); // TODO: We don't really need this log

            return true;
        } else {
            ChestRestack.log(Level.WARNING, "Could not load PlayerPreferences from file. Creating new PlayerPreferences.");
        }

        return false;
    }

    public Set<Material> getMaterialsToNotMove() {
        Set<Material> materials = new HashSet<>();

        if (!isMoveTools())
            materials.addAll(toolsToNotMove);
        if (!isMoveArmor())
            materials.addAll(armorToNotMove);
        if (!isMoveArrows())
            materials.addAll(arrowsToNotMove);

        return materials;
    }

    public static Set<Material> toolsToNotMove = Set.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.BOW, Material.CROSSBOW, Material.TOTEM_OF_UNDYING, Material.TRIDENT, Material.SHIELD, Material.FLINT_AND_STEEL, Material.SHEARS, Material.FISHING_ROD,
            Material.CLOCK, Material.COMPASS, Material.RECOVERY_COMPASS, Material.LEAD, Material.CARROT_ON_A_STICK, Material.BRUSH, Material.ELYTRA, Material.WARPED_FUNGUS_ON_A_STICK
    );

    public static Set<Material> armorToNotMove = Set.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
            Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,
            Material.TURTLE_HELMET, Material.ELYTRA, Material.SHIELD
    );

    public static Set<Material> arrowsToNotMove = Set.of(
            Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW, Material.FIREWORK_ROCKET
    );

    public static String filePathForUUID(UUID id) {
        return ChestRestack.getUserdataPath() + "/" + id + ".data";
    }

    /**
     * Returns a minedown formatted message that gets displayed when the user uses the "/chestrestack preferences" command.
     * This message also allows the user to change their settings by clicking toggles.
     * @return the formatted minedown message
     */
    public String getMessage() {
        StringBuilder sb = new StringBuilder("\n" + MessagesConfig.getMessage("commands.preferences.header"));

        sb.append("\n").append(getRestackingMessage());
        if (Config.getSortingEnabledGlobal()) {
            sb.append("\n").append(getSortingMessage());
        }
        sb.append("\n").append(getHotbarMessage());
        sb.append("\n").append(getToolsMessage());
        sb.append("\n").append(getArmorMessage());
        sb.append("\n").append(getArrowsMessage());
        sb.append("\n").append(getClickmodeMessage());
        sb.append("\n").append(MessagesConfig.getMessage("commands.preferences.footer"));
        return sb.toString();
    }

    public String getRestackingMessage() {
        String restackingToggle = MessagesConfig.getMessage(enabled ? "preferences.restacking-toggle-enabled" : "preferences.restacking-toggle-disabled");
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.restacking"), restackingToggle);
    }

    public String getClickmodeMessage() {
        String clickModeToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.clickmode-toggle"), MessagesConfig.getMessage(clickMode.messageKey()));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.clickmode"), clickModeToggle);
    }

    public String getSortingMessage() {
        String sortingToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.sorting-toggle"), MessagesConfig.getMessage(sortingEnabled ? "preferences.enabled" : "preferences.disabled"));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.sorting"), sortingToggle);
    }

    public String getHotbarMessage() {
        String hotbarToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.hotbar-toggle"), MessagesConfig.getMessage(moveFromHotbar ? "preferences.enabled" : "preferences.disabled"));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.hotbar"), hotbarToggle);
    }

    public String getToolsMessage() {
        String toolsToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.tools-toggle"), MessagesConfig.getMessage(moveTools ? "preferences.enabled" : "preferences.disabled"));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.tools"), toolsToggle);
    }

    public String getArmorMessage() {
        String armorToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.armor-toggle"), MessagesConfig.getMessage(moveArmor ? "preferences.enabled" : "preferences.disabled"));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.armor"), armorToggle);
    }

    public String getArrowsMessage() {
        String arrowsToggle = MessageFormat.format(MessagesConfig.getMessage("preferences.arrows-toggle"), MessagesConfig.getMessage(moveArrows ? "preferences.enabled" : "preferences.disabled"));
        return MessageFormat.format(MessagesConfig.getMessage("commands.preferences.arrows"), arrowsToggle);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ClickMode getClickMode() {
        return clickMode;
    }

    public void setClickMode(ClickMode clickMode) {
        this.clickMode = clickMode;
    }

    public boolean isSortingEnabled() {
        return sortingEnabled;
    }

    public void setSortingEnabled(boolean sortingEnabled) {
        this.sortingEnabled = sortingEnabled;
    }

    public boolean isMoveFromHotbar() {
        return moveFromHotbar;
    }

    public void setMoveFromHotbar(boolean moveFromHotbar) {
        this.moveFromHotbar = moveFromHotbar;
    }

    public boolean isMoveTools() {
        return moveTools;
    }

    public void setMoveTools(boolean moveTools) {
        this.moveTools = moveTools;
    }

    public boolean isMoveArmor() {
        return moveArmor;
    }

    public void setMoveArmor(boolean moveArmor) {
        this.moveArmor = moveArmor;
    }

    public boolean isMoveArrows() {
        return moveArrows;
    }

    public void setMoveArrows(boolean moveArrows) {
        this.moveArrows = moveArrows;
    }
}
