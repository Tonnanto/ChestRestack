package de.stamme.chestrestack;

import de.stamme.chestrestack.model.RestackResult;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ServerInfo implements Serializable {

    // Singleton
    private static ServerInfo instance;

    private ServerInfo() {
        itemsRestackedMap = new HashMap<>();
    }

    public static ServerInfo getInstance() {
        if (instance != null) return instance;
        ServerInfo loadedInfo = load();
        if (loadedInfo != null) instance = loadedInfo;
        else instance = new ServerInfo();
        return instance;
    }


    // Attributes
    private static final String path = ChestRestack.getPlugin().getDataFolder() + "/server_info.data";

    private Map<Long, Integer> itemsRestackedMap;

    public int getRestackCount() {
        removeOldRestacks();
        return itemsRestackedMap.size();
    }

    public int getRestackedItemsCount() {
        removeOldRestacks();
        if (itemsRestackedMap.values().stream().reduce(Integer::sum).isEmpty()) {
            return 0;
        }
        return itemsRestackedMap.values().stream().reduce(Integer::sum).get();
    }

    private void removeOldRestacks() {
        long currentTimestamp = System.currentTimeMillis();
        for (Long restackTimestamp : itemsRestackedMap.keySet()) {
            // Remove if more than one week ago
            if (currentTimestamp - restackTimestamp > 7 * 24 * 60 * 60 * 1000) {
                itemsRestackedMap.remove(restackTimestamp);
            }
        }
    }

    public static void save() {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)));
            out.writeObject(getInstance());
            out.flush();
            out.close();

        } catch (Exception e) {
            ChestRestack.log(Level.SEVERE, e.getMessage());
        }
    }

    private static ServerInfo load() {
        Object obj = null;

        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(path)));
            obj = in.readObject();
        } catch (Exception ignored) {
        }

        if (obj instanceof ServerInfo)
            return (ServerInfo) obj;
        else return null;
    }

    public void logRestackResult(RestackResult result) {
        itemsRestackedMap.put(System.currentTimeMillis(), result.numberOfItemsMoved());
    }
}
