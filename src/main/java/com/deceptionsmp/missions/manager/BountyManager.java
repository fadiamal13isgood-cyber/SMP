package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyManager {

    private final DeceptionMissions plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<UUID, List<ItemStack>> activeBounties = new HashMap<>();
    private final Map<UUID, List<ItemStack>> pendingClaims = new HashMap<>();

    public BountyManager(DeceptionMissions plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bounties.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        loadBounties();
    }

    public void addBounty(UUID targetUuid, List<ItemStack> items) {
        activeBounties.computeIfAbsent(targetUuid, k -> new ArrayList<>()).addAll(items);
        saveBounties();
    }

    public boolean hasBounty(UUID targetUuid) {
        return activeBounties.containsKey(targetUuid) && !activeBounties.get(targetUuid).isEmpty();
    }

    public void handleKill(Player killer, Player target) {
        UUID targetUuid = target.getUniqueId();
        if (!hasBounty(targetUuid)) return;

        List<ItemStack> rewardItems = activeBounties.remove(targetUuid);
        if (rewardItems != null && !rewardItems.isEmpty()) {
            pendingClaims.computeIfAbsent(killer.getUniqueId(), k -> new ArrayList<>()).addAll(rewardItems);
            saveBounties();
        }
    }

    public void openClaimGui(Player player) {
        UUID uuid = player.getUniqueId();
        List<ItemStack> items = pendingClaims.get(uuid);

        if (items == null || items.isEmpty()) {
            player.sendMessage("§cYou have no bounty rewards to claim!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54, "Claim Bounty Rewards");
        for (ItemStack item : items) {
            if (item != null) gui.addItem(item);
        }
        player.openInventory(gui);
    }

    public void saveClaimGui(Player player, Inventory gui) {
        UUID uuid = player.getUniqueId();
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack item : gui.getContents()) {
            if (item != null) remaining.add(item);
        }

        if (remaining.isEmpty()) {
            pendingClaims.remove(uuid);
        } else {
            pendingClaims.put(uuid, remaining);
        }
        saveBounties();
    }

    public void loadBounties() {
        activeBounties.clear();
        pendingClaims.clear();

        if (config.contains("active")) {
            for (String key : config.getConfigurationSection("active").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                List<ItemStack> items = (List<ItemStack>) config.get("active." + key);
                if (items != null) activeBounties.put(uuid, new ArrayList<>(items));
            }
        }

        if (config.contains("claims")) {
            for (String key : config.getConfigurationSection("claims").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                List<ItemStack> items = (List<ItemStack>) config.get("claims." + key);
                if (items != null) pendingClaims.put(uuid, new ArrayList<>(items));
            }
        }
    }

    public void saveBounties() {
        config.set("active", null);
        config.set("claims", null);

        activeBounties.forEach((uuid, items) -> config.set("active." + uuid.toString(), items));
        pendingClaims.forEach((uuid, items) -> config.set("claims." + uuid.toString(), items));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
