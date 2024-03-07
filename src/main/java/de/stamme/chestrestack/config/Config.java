package de.stamme.chestrestack.config;

import de.stamme.chestrestack.ChestRestack;
import de.stamme.chestrestack.model.PlayerPreferences;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    static FileConfiguration config;
    static String configPath;
    static Pattern versionPattern;

    public static void register() {
        configPath = ChestRestack.getPlugin().getDataFolder() + File.separator + "config.yml";
        versionPattern = Pattern.compile("version [0-9.]+\\b");

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            // No config file exists
            ChestRestack.getPlugin().saveDefaultConfig();
            ChestRestack.getPlugin().reloadConfig();
            config = ChestRestack.getPlugin().getConfig();
            return;
        }

        config = ChestRestack.getPlugin().getConfig();

        // Reading old config.yml
        String configString = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(configPath));
            configString = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ChestRestack.log(Level.SEVERE, e.getMessage());
            ChestRestack.log(Level.SEVERE, "Failed to read old config file");
        }

        // Looking for version String in file
        Matcher m = versionPattern.matcher(configString);
        if (m.find()) {
            String configVersionString = m.group();
            if (configVersionString.equalsIgnoreCase(getCurrentVersionString())) {
                // Config is up-to-date!
                return;
            }
        }
        // Config file needs to be updated
        migrateConfig(configFile);
    }

    /**
     * Migrates an existing config file to a new version
     * 1. Reads old config values
     * 2. Deletes old config file
     * 3. Creates new default config file
     * 4. Injects old values into new file
     *
     * @param oldFile the file to migrate from
     */
    private static void migrateConfig(File oldFile) {
        // Keep old config values to overwrite in new config file
        Map<String, Object> oldValues = config.getValues(true);

        // Delete old config.yml
        if (!oldFile.delete()) {
            ChestRestack.log(Level.SEVERE, "Failed to delete outdated config.yml");
        }

        // Save new default config.yml
        ChestRestack.getPlugin().saveDefaultConfig();

        // Reading new config.yml
        String configString = "";
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(configPath));
            configString = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ChestRestack.log(Level.SEVERE, e.getMessage());
            ChestRestack.log(Level.SEVERE, "Failed to read new config.yml file");
        }

        // Replace values in new config.yml with old values
        for (Map.Entry<String, Object> configValue : oldValues.entrySet()) {
            Object obj = configValue.getValue();

            if (obj instanceof List) {
                // Replace multiline / list values

                Pattern keyPat = Pattern.compile(configValue.getKey() + ":\\n^\\s*- .*$(\\n^\\s*- .*$)*", Pattern.MULTILINE);
                // Matches consecutive lines starting with "  - " aka lists in the yaml file
                Matcher matcher = keyPat.matcher(configString);
                StringBuilder configValueString = new StringBuilder(configValue.getKey() + ": ");
                ((List<?>) obj).forEach(o -> configValueString.append("\n  - ").append(o.toString()));
                configString = matcher.replaceAll(configValueString.toString());

            } else {
                // Replace single line values
                Pattern keyPat = Pattern.compile(configValue.getKey() + ":.+\\b");
                configString = keyPat.matcher(configString).replaceAll(configValue.getKey() + ": " + obj.toString());
            }
        }
        configString = versionPattern.matcher(configString).replaceFirst(getCurrentVersionString());

        // Save new config.yml with replaced values
        File newConfig = new File(configPath);
        try {
            FileWriter fw = new FileWriter(newConfig, false);
            fw.write(configString);
            fw.close();

        } catch (IOException e) {
            ChestRestack.log(Level.SEVERE, e.getMessage());
            ChestRestack.log(Level.SEVERE, "Failed to write to new config.yml file");
            return;
        }

        ChestRestack.getPlugin().reloadConfig();
        Config.config = ChestRestack.getPlugin().getConfig();

        ChestRestack.log("config.yml has been updated to " + getCurrentVersionString());
    }

    /**
     * @return the current version string in config files
     */
    private static String getCurrentVersionString() {
        String version = ChestRestack.getPlugin().getDescription().getVersion();
        return "version " + version;
    }

    /**
     * Reload the plugin configuration.
     */
    public static void reload() {
        ChestRestack.getPlugin().reloadConfig();
        config = ChestRestack.getPlugin().getConfig();
    }

    /**
     * Retrieve whether to check for updates
     *
     * @return boolean
     */
    public static boolean checkForUpdates() {
        return config.getBoolean("check-for-updates", true);
    }

    /**
     * Retrieve the plugin locale.
     *
     * @return String
     */
    public static String getLocale() {
        return config.getString("locale", "en");
    }

    /**
     * Retrieve the Minecraft item locale.
     *
     * @return String
     */
    public static String getMinecraftItemsLocale() {
        String locale = config.getString("minecraft-items-locale");

        return locale == null || locale.equalsIgnoreCase("en_us") ? null : locale;
    }

    /**
     * Retrieve the Minecraft items locale update interval.
     *
     * @return int
     */
    public static int getMinecraftItemsLocaleUpdatePeriod() {
        return 7;
    }

    /**
     * Retrieve the "sorting-enabled-global" config flag.
     *
     * @return boolean
     */
    public static boolean getSortingEnabledGlobal() {
        return config.getBoolean("sorting-enabled-global", true);
    }

    /**
     * Retrieve the "restack-sound" config flag.
     *
     * @return boolean
     */
    public static boolean getRestackSoundEnabled() {
        return config.getBoolean("restack-sound", true);
    }

    /**
     * Retrieve a default PlayerPreferences object based on the default preferences found in the config
     *
     * @return PlayerPreferences
     */
    public static PlayerPreferences getDefaultPlayerPreferences() {
        PlayerPreferences.ClickMode defaultClickMode = PlayerPreferences.ClickMode.SHIFT_RIGHT;
        try {
            defaultClickMode = PlayerPreferences.ClickMode.valueOf(config.getString("click-mode-default", "shift-right").replace("-", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            ChestRestack.log(Level.WARNING, "Config option click-mode-default is badly formatted. Defaulting to shift-right.");
        }
        return new PlayerPreferences(
                true,
                defaultClickMode,
                config.getBoolean("sorting-enabled-default", true),
                config.getBoolean("move-from-hotbar-default", true),
                config.getBoolean("move-tools-default", false),
                config.getBoolean("move-armor-default", true),
                config.getBoolean("move-arrows-default", true),
                config.getBoolean("move-food-default", true),
                config.getBoolean("move-potions-default", true)
        );
    }
}
