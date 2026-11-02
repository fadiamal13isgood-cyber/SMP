package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import com.deceptionsmp.missions.model.MissionDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;

public class MissionManager {

    private final DeceptionMissions plugin;
    private final Map<UUID, Mission> activeMissions = new HashMap<>();
    private final Map<UUID, String> lastCompletedMissionId = new HashMap<>();
    private final Random random = new Random();

    public MissionManager(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    public Mission getActiveMission(UUID playerUuid) {
        return activeMissions.get(playerUuid);
    }

    public void assignRandomMission(Player player) {
        List<MissionPoolData.MissionTemplate> availablePool = MissionPoolData.getPool();
        String lastId = lastCompletedMissionId.get(player.getUniqueId());

        List<MissionPoolData.MissionTemplate> filteredPool = new ArrayList<>();
        for (MissionPoolData.MissionTemplate t : availablePool) {
            if (!t.id().equalsIgnoreCase(lastId)) {
                filteredPool.add(t);
            }
        }

        if (filteredPool.isEmpty()) filteredPool = availablePool;

        MissionPoolData.MissionTemplate chosen = filteredPool.get(random.nextInt(filteredPool.size()));
        long deadline = System.currentTimeMillis() + (chosen.difficulty() == MissionPoolData.MissionDifficulty.EASY ? 1800000L : 3600000L);

        Mission mission = new Mission(
                chosen.id(),
                MissionDifficulty.valueOf(chosen.difficulty().name()),
                chosen.type().name(),
                chosen.targetMaterial().name(),
                chosen.requiredAmount(),
                deadline
        );

        activeMissions.put(player.getUniqueId(), mission);
        playAssignmentAnimation(player, mission);
    }

    private void playAssignmentAnimation(Player player, Mission mission) {
        String[] fakeMissions = {
            "GATHER 128 COBBLESTONE", "ELIMINATE AN ENDERMAN", "CRAFT A BEACON",
            "MINE 32 ANCIENT DEBRIS", "TAME 5 WOLVES", "FISH 15 RAW SALMON",
            "BREW 3 TURTLE MASTER POTIONS", "KILL A WITHER", "EXPLORE AN ANCIENT CITY"
        };

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 30;

            @Override
            public void run() {
                if (ticks < maxTicks) {
                    if (ticks % 2 == 0) {
                        String fake = fakeMissions[random.nextInt(fakeMissions.length)];
                        player.sendActionBar(Component.text("DECRYPTING MISSION: " + fake, NamedTextColor.DARK_GRAY));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f + (ticks * 0.02f));
                    }
                    ticks++;
                } else {
                    player.sendActionBar(Component.text("MISSION ASSIGNED: " + mission.getDescription(), NamedTextColor.RED, TextDecoration.BOLD));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 0.8f);
                    player.showTitle(Title.title(
                            Component.text("NEW SECRET MISSION", NamedTextColor.RED, TextDecoration.BOLD),
                            Component.text(mission.getDescription(), NamedTextColor.GRAY),
                            Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(2000), Duration.ofMillis(500))
                    ));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void checkProgress(Player player, String missionTypeStr, int amount, String target) {
        Mission mission = activeMissions.get(player.getUniqueId());
        if (mission == null) return;

        boolean typeMatch = mission.getType() != null && mission.getType().equalsIgnoreCase(missionTypeStr);
        String missionTargetStr = mission.getTargetMaterial() != null ? mission.getTargetMaterial() : "";
        
        // Match target material or entity name (case insensitive)
        boolean targetMatch = target == null || target.isEmpty() || missionTargetStr.equalsIgnoreCase(target);

        if (typeMatch && targetMatch) {
            mission.setProgress(mission.getProgress() + amount);
            if (mission.getProgress() >= mission.getRequiredAmount()) {
                completeMission(player, mission);
            } else {
                player.sendActionBar(Component.text("Mission Progress: " + mission.getProgress() + " / " + mission.getRequiredAmount(), NamedTextColor.YELLOW));
            }
        }
    }

    public void failMission(UUID playerUuid, Mission mission) {
        activeMissions.remove(playerUuid);
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(Component.text("[MISSION FAILED] Time ran out or session expired.", NamedTextColor.RED));
            assignRandomMission(player);
        }
    }

    public void completeMission(Player player, Mission mission) {
        activeMissions.remove(player.getUniqueId());
        lastCompletedMissionId.put(player.getUniqueId(), mission.getId());

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GREEN));
        player.sendMessage(Component.text(" MISSION COMPLETE: ", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(Component.text(mission.getDescription(), NamedTextColor.WHITE)));

        rewardPlayer(player, mission.getDifficulty());

        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_GREEN));

        Bukkit.getScheduler().runTaskLater(plugin, () -> assignRandomMission(player), 100L);
    }

    private void rewardPlayer(Player player, MissionDifficulty difficulty) {
        // 0.5% Chance Jackpot Reward: Max Hearts Upgrade
        if (random.nextDouble() * 100.0 <= 0.5) {
            AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                double currentMax = maxHealthAttr.getBaseValue();
                if (currentMax < 40.0) {
                    int heartsWon = random.nextInt(3) + 1;
                    double addedHp = Math.min(heartsWon * 2.0, 40.0 - currentMax);
                    maxHealthAttr.setBaseValue(currentMax + addedHp);

                    player.sendMessage(Component.text("★ JACKPOT REWARD! ", NamedTextColor.GOLD, TextDecoration.BOLD)
                            .append(Component.text("You gained +" + (addedHp / 2.0) + " Max Hearts! (Cap: 20)", NamedTextColor.LIGHT_PURPLE)));
                    return;
                }
            }
        }

        // Material / Custom Items Reward
        List<ItemStack> rewards = new ArrayList<>();
        if (difficulty == MissionDifficulty.EASY) {
            rewards.add(new ItemStack(org.bukkit.Material.IRON_INGOT, 8 + random.nextInt(9)));
            rewards.add(new ItemStack(org.bukkit.Material.GOLD_INGOT, 4 + random.nextInt(5)));
        } else if (difficulty == MissionDifficulty.MEDIUM) {
            rewards.add(new ItemStack(org.bukkit.Material.DIAMOND, 3 + random.nextInt(4)));
            rewards.add(new ItemStack(org.bukkit.Material.GOLDEN_APPLE, 2 + random.nextInt(3)));
        } else {
            rewards.add(new ItemStack(org.bukkit.Material.DIAMOND, 6 + random.nextInt(7)));
            rewards.add(new ItemStack(org.bukkit.Material.GOLDEN_APPLE, 4 + random.nextInt(5)));
        }

        player.sendMessage(Component.text("REWARDS GAINED:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        for (ItemStack item : rewards) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(Component.text(" • " + item.getAmount() + "x " + item.getType().name().replace("_", " "), NamedTextColor.AQUA));
        }

        applyQuestBuffs(player, difficulty);
    }

    private void applyQuestBuffs(Player player, MissionDifficulty difficulty) {
        player.sendMessage(Component.text("BUFFS ACQUIRED:", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

        // 5% God-Tier Buff Chance
        if (random.nextDouble() * 100.0 <= 5.0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 6000, 3, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 3600, 1, true, true));
            player.sendMessage(Component.text(" • ⚡ GOD-TIER BUFF OVERLOAD! Absorption IV, Fire Res & Jump Boost II (5:00)", NamedTextColor.GOLD));
            return;
        }

        if (difficulty == MissionDifficulty.EASY) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 0, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 2400, 0, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 1200, 0, true, true));
            player.sendMessage(Component.text(" • Speed I (2:00), Night Vision (2:00) & Haste I (1:00)", NamedTextColor.AQUA));

        } else if (difficulty == MissionDifficulty.MEDIUM) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 3600, 1, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 0, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 2400, 0, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 1200, 0, true, true));
            player.sendMessage(Component.text(" • Haste II (3:00), Regen I (0:30), Water Breathing & Dolphin's Grace", NamedTextColor.AQUA));

        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 3600, 1, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 2400, 1, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 3600, 1, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 2400, 0, true, true));
            player.sendMessage(Component.text(" • Strength II (3:00), Resistance II (2:00), Health Boost & Fire Res", NamedTextColor.AQUA));
        }
    }

    public void startDeadlineChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (UUID uuid : new HashSet<>(activeMissions.keySet())) {
                    Mission m = activeMissions.get(uuid);
                    if (m != null && now >= m.getDeadlineTimestamp()) {
                        failMission(uuid, m);
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }
}
