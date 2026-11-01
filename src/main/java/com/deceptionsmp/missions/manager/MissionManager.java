package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import com.deceptionsmp.missions.model.MissionDifficulty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MissionManager {

    private final DeceptionMissions plugin;
    private final Map<UUID, Mission> activeMissions = new HashMap<>();

    public MissionManager(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    public Mission getActiveMission(UUID uuid) {
        return activeMissions.get(uuid);
    }

    public void setActiveMission(UUID uuid, Mission mission) {
        activeMissions.put(uuid, mission);
    }

    public void assignRandomMission(Player player) {
        Random rng = new Random();
        MissionDifficulty[] diffs = MissionDifficulty.values();
        MissionDifficulty diff = diffs[rng.nextInt(diffs.length)];

        int type = rng.nextInt(3);
        Mission mission;
        String id = UUID.randomUUID().toString().substring(0, 8);
        long deadline = System.currentTimeMillis() + (diff.getTimeLimitHours() * 3600000L);

        if (type == 0) {
            mission = new Mission(id, diff, "KILL_PLAYERS", "Eliminate " + (diff.ordinal() + 1) + " enemy player(s)", diff.ordinal() + 1, deadline);
        } else if (type == 1) {
            Material mat = (diff == MissionDifficulty.CLASSIFIED) ? Material.NETHERITE_INGOT : Material.DIAMOND;
            int count = (diff == MissionDifficulty.CLASSIFIED) ? 2 : (diff.ordinal() + 1) * 4;
            mission = new Mission(id, diff, "COLLECT_ITEMS", "Gather " + count + " " + mat.name(), count, deadline);
            mission.setTargetMaterial(mat.name());
        } else {
            int dist = (diff.ordinal() + 1) * 1000;
            mission = new Mission(id, diff, "EXPLORE", "Travel " + dist + " blocks from world spawn", dist, deadline);
        }

        activeMissions.put(player.getUniqueId(), mission);
        player.sendMessage(ChatColor.DARK_RED + "[DECEPTION] " + ChatColor.RED + "A new secret mission has been assigned! Type /mission to view.");
    }

    public void checkProgress(Player player, String type, int amount, String targetExtra) {
        Mission m = activeMissions.get(player.getUniqueId());
        if (m == null || !m.getType().equalsIgnoreCase(type)) return;

        if (type.equals("COLLECT_ITEMS") && !m.getTargetMaterial().equalsIgnoreCase(targetExtra)) return;

        m.setProgress(m.getProgress() + amount);
        if (m.getProgress() >= m.getRequiredAmount()) {
            completeMission(player, m);
        }
    }

    public void completeMission(Player player, Mission m) {
        activeMissions.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "[DECEPTION] Mission Completed: " + m.getDescription());
        plugin.getRewardManager().giveRewards(player, m.getDifficulty());
        plugin.getStorageManager().addXp(player.getUniqueId(), m.getDifficulty().getXpReward());
        assignRandomMission(player);
    }

    public void failMission(UUID uuid, Mission m) {
        activeMissions.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(ChatColor.RED + "[DECEPTION] Mission Failed! Penalty applied.");
            plugin.getRewardManager().applyPunishment(player, m.getDifficulty());
            assignRandomMission(player);
        }
    }

    public void startDeadlineChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                new HashMap<>(activeMissions).forEach((uuid, mission) -> {
                    if (now >= mission.getDeadlineTimestamp()) {
                        failMission(uuid, mission);
                    }
                });
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }
}
