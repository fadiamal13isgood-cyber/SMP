package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import com.deceptionsmp.missions.model.MissionDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MissionManager {

    private final DeceptionMissions plugin;
    private final Map<UUID, Mission> activeMissions = new HashMap<>();

    private final String[] dummyTitles = {
        "Assassinate a Target",
        "Steal Netherite Gear",
        "Infiltrate Base Alpha",
        "Explore 5000 Blocks",
        "Protect VIP Player",
        "Collect 32 Diamonds",
        "Survive 72 Hours",
        "Raid Hidden Vault"
    };

    public MissionManager(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    public Mission getActiveMission(UUID uuid) {
        return activeMissions.get(uuid);
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
            mission = new Mission(id, diff, "EXPLORE", "Travel " + dist + " blocks from spawn", dist, deadline);
        }

        activeMissions.put(player.getUniqueId(), mission);
        playSpinAnimation(player, mission);
    }

    private void playSpinAnimation(Player player, Mission finalMission) {
        new BukkitRunnable() {
            int step = 0;
            int delayTicks = 1;
            int counter = 0;
            final Random rand = new Random();

            @Override
            public void run() {
                counter++;
                if (counter < delayTicks) return;
                counter = 0;

                if (step < 20) {
                    String randomTitle = dummyTitles[rand.nextInt(dummyTitles.length)];
                    player.sendActionBar(Component.text("ASSIGNING: " + randomTitle, NamedTextColor.GOLD, TextDecoration.BOLD));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);

                    step++;
                    if (step > 12) delayTicks = 3;
                    if (step > 16) delayTicks = 6;
                } else {
                    player.sendActionBar(Component.text("MISSION: " + finalMission.getDescription(), NamedTextColor.RED, TextDecoration.BOLD));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    player.sendMessage(Component.text("\n[DECEPTION MISSION ASSIGNED]", NamedTextColor.DARK_RED, TextDecoration.BOLD));
                    player.sendMessage(Component.text("Objective: ", NamedTextColor.YELLOW).append(Component.text(finalMission.getDescription(), NamedTextColor.WHITE)));
                    player.sendMessage(Component.text("Difficulty: ", NamedTextColor.GOLD).append(Component.text(finalMission.getDifficulty().name(), NamedTextColor.WHITE)));
                    player.sendMessage(Component.text("Completion Reward: ", NamedTextColor.GREEN).append(Component.text("+" + finalMission.getDifficulty().getXpReward() + " XP Rank Progress", NamedTextColor.WHITE)));
                    player.sendMessage(Component.text("Type /mission to check your remaining time.\n", NamedTextColor.GRAY));

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
        player.sendMessage(Component.text("[DECEPTION] Mission Completed: " + m.getDescription(), NamedTextColor.GREEN));
        plugin.getRewardManager().giveRewards(player, m.getDifficulty());
        plugin.getStorageManager().addXp(player.getUniqueId(), m.getDifficulty().getXpReward());
        assignRandomMission(player);
    }

    public void failMission(UUID uuid, Mission m) {
        activeMissions.remove(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
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
