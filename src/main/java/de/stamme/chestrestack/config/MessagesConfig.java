package de.stamme.chestrestack.config;

import de.stamme.chestrestack.ChestRestack;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MessagesConfig {
    private static FileConfiguration customMessages;
    private static FileConfiguration defaultMessages;

    /**
     * Register the messages configuration.
     */
    public static void register(String locale) {
        ChestRestack plugin = ChestRestack.getPlugin();
        String filePath = "custom_messages.yml";

        File messagesFile = new File(
            plugin.getDataFolder(),
            filePath
        );

        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource(filePath, false);
        }

        customMessages = YamlConfiguration.loadConfiguration(messagesFile);
        defaultMessages = getDefaultMessages(locale);
    }

    private static FileConfiguration getDefaultMessages(String locale) {
        if (locale.contains("_")) {
            locale = locale.split("_")[0];
        }
        ChestRestack plugin = ChestRestack.getPlugin();
        String filePath = "lang/messages_" + locale + ".yml";

        ClassLoader classLoader = plugin.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filePath);

        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + filePath);
        }

        try (
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)
        ) {
            FileConfiguration config = new YamlConfiguration();
            config.load(reader);
            return config;

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve the localized message.
     *
     * @param key The message key.
     * @return String
     */
    public static String getMessage(String key) {
        String message = getCustomMessages().getString(key);

        if (message == null && getDefaultMessages() != null) {
            message = getDefaultMessages().getString(key);
        }

        return ChatColor.translateAlternateColorCodes(
            '&',
            message == null ? key + " is missing." : message
        );
    }

    /**
     * Determine whether the message key exists.
     *
     * @param key The message key.
     * @return boolean
     */
    public static boolean hasKey(String key) {
        String message = getCustomMessages().getString(key);
        if (getDefaultMessages() != null && (message == null || message.isEmpty()))
            message = getDefaultMessages().getString(key);
        return message != null && !message.isEmpty();
    }


    /**
     * Retrieve the messages configuration.
     *
     * @return FileConfiguration
     */
    private static FileConfiguration getCustomMessages() {
        return customMessages;
    }

    /**
     * Retrieve the messages configuration.
     *
     * @return FileConfiguration
     */
    @Nullable
    private static FileConfiguration getDefaultMessages() {
        return defaultMessages;
    }

    /**
     * Retrieve whether the server uses custom messages
     *
     * @return boolean
     */
    public static boolean usesCustomMessages() {
        return !customMessages.getKeys(true).isEmpty();
    }
}
