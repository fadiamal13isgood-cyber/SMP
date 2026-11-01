package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorageManager {

    private final DeceptionMissions plugin;
    private final File file;
    private final FileConfiguration config;

    private final Map<UUID, Integer> intelMap = new HashMap<>();
    private final Map<UUID, Integer> creditsMap = new HashMap<>();
    private final Map<UUID, Integer> tokensMap = new HashMap<>();
    private final Map<UUID, Integer> xpMap = new HashMap<>();

    public StorageManager(DeceptionMissions plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAllData();
    }

    public void loadAllData() {
        if (!config.contains("players")) return;
        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            intelMap.put(uuid, config.getInt("players." + key + ".intel", 0));
            creditsMap.put(uuid, config.getInt("players." + key + ".credits", 0));
            tokensMap.put(uuid, config.getInt("players." + key + ".tokens", 0));
            xpMap.put(uuid, config.getInt("players." + key + ".xp", 0));
        }
    }

    public void saveAllData() {
        for (UUID uuid : xpMap.keySet()) {
            String path = "players." + uuid.toString() + ".";
            config.set(path + "intel", getIntel(uuid));
            config.set(path + "credits", getCredits(uuid));
            config.set(path + "tokens", getTokens(uuid));
            config.set(path + "xp", getXp(uuid));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getIntel(UUID uuid) { return intelMap.getOrDefault(uuid, 0); }
    public int getCredits(UUID uuid) { return creditsMap.getOrDefault(uuid, 0); }
    public int getTokens(UUID uuid) { return tokensMap.getOrDefault(uuid, 0); }
    public int getXp(UUID uuid) { return xpMap.getOrDefault(uuid, 0); }

    public void addIntel(UUID uuid, int amt) { intelMap.put(uuid, getIntel(uuid) + amt); }
    public void addCredits(UUID uuid, int amt) { creditsMap.put(uuid, getCredits(uuid) + amt); }
    public void addTokens(UUID uuid, int amt) { tokensMap.put(uuid, getTokens(uuid) + amt); }
    public void addXp(UUID uuid, int amt) { xpMap.put(uuid, getXp(uuid) + amt); }
}
