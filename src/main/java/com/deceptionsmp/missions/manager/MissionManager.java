package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import com.deceptionsmp.missions.model.MissionDifficulty;
import com.deceptionsmp.missions.model.MissionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        List<MissionTemplate> availablePool = getExpandedMissionPool();
        String lastId = lastCompletedMissionId.get(player.getUniqueId());

        List<MissionTemplate> filteredPool = new ArrayList<>();
        for (MissionTemplate t : availablePool) {
            if (!t.id().equalsIgnoreCase(lastId)) {
                filteredPool.add(t);
            }
        }

        if (filteredPool.isEmpty()) filteredPool = availablePool;

        MissionTemplate chosen = filteredPool.get(random.nextInt(filteredPool.size()));
        long deadline = System.currentTimeMillis() + (chosen.difficulty() == MissionDifficulty.EASY ? 1800000L : 3600000L);

        Mission mission = new Mission(
                chosen.id(),
                player.getUniqueId(),
                chosen.type(),
                chosen.difficulty(),
                chosen.description(),
                chosen.targetMaterial(),
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

        List<ItemStack> rewards = new ArrayList<>();
        if (difficulty == MissionDifficulty.EASY) {
            rewards.add(new ItemStack(Material.IRON_INGOT, 8 + random.nextInt(9)));
            rewards.add(new ItemStack(Material.GOLD_INGOT, 4 + random.nextInt(5)));
        } else if (difficulty == MissionDifficulty.MEDIUM) {
            rewards.add(new ItemStack(Material.DIAMOND, 3 + random.nextInt(4)));
            rewards.add(new ItemStack(Material.GOLDEN_APPLE, 2 + random.nextInt(3)));
        } else {
            rewards.add(new ItemStack(Material.DIAMOND, 6 + random.nextInt(7)));
            rewards.add(new ItemStack(Material.GOLDEN_APPLE, 4 + random.nextInt(5)));
        }

        player.sendMessage(Component.text("REWARDS GAINED:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        for (ItemStack item : rewards) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(Component.text(" • " + item.getAmount() + "x " + item.getType().name().replace("_", " "), NamedTextColor.AQUA));
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
                        Player player = Bukkit.getPlayer(uuid);
                        activeMissions.remove(uuid);
                        if (player != null && player.isOnline()) {
                            player.sendMessage(Component.text("[MISSION EXPIRED] You failed to complete your mission in time!", NamedTextColor.RED));
                            assignRandomMission(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private List<MissionTemplate> getExpandedMissionPool() {
        List<MissionTemplate> pool = new ArrayList<>();

        // EASY GATHERING (15)
        pool.add(new MissionTemplate("g_cobble", MissionType.GATHER, MissionDifficulty.EASY, "Mine 128 Cobblestone", Material.COBBLESTONE, 128));
        pool.add(new MissionTemplate("g_coal", MissionType.GATHER, MissionDifficulty.EASY, "Mine 32 Coal", Material.COAL, 32));
        pool.add(new MissionTemplate("g_iron_raw", MissionType.GATHER, MissionDifficulty.EASY, "Mine 32 Raw Iron", Material.RAW_IRON, 32));
        pool.add(new MissionTemplate("g_copper", MissionType.GATHER, MissionDifficulty.EASY, "Mine 48 Raw Copper", Material.RAW_COPPER, 48));
        pool.add(new MissionTemplate("g_wood", MissionType.GATHER, MissionDifficulty.EASY, "Chop 64 Oak Logs", Material.OAK_LOG, 64));
        pool.add(new MissionTemplate("g_birch", MissionType.GATHER, MissionDifficulty.EASY, "Chop 64 Birch Logs", Material.BIRCH_LOG, 64));
        pool.add(new MissionTemplate("g_spruce", MissionType.GATHER, MissionDifficulty.EASY, "Chop 64 Spruce Logs", Material.SPRUCE_LOG, 64));
        pool.add(new MissionTemplate("g_sand", MissionType.GATHER, MissionDifficulty.EASY, "Gather 64 Sand", Material.SAND, 64));
        pool.add(new MissionTemplate("g_gravel", MissionType.GATHER, MissionDifficulty.EASY, "Gather 64 Gravel", Material.GRAVEL, 64));
        pool.add(new MissionTemplate("g_dirt", MissionType.GATHER, MissionDifficulty.EASY, "Gather 128 Dirt", Material.DIRT, 128));
        pool.add(new MissionTemplate("g_wheat", MissionType.GATHER, MissionDifficulty.EASY, "Harvest 32 Wheat", Material.WHEAT, 32));
        pool.add(new MissionTemplate("g_carrots", MissionType.GATHER, MissionDifficulty.EASY, "Harvest 32 Carrots", Material.CARROT, 32));
        pool.add(new MissionTemplate("g_potatoes", MissionType.GATHER, MissionDifficulty.EASY, "Harvest 32 Potatoes", Material.POTATO, 32));
        pool.add(new MissionTemplate("g_sugar_cane", MissionType.GATHER, MissionDifficulty.EASY, "Harvest 32 Sugar Cane", Material.SUGAR_CANE, 32));
        pool.add(new MissionTemplate("g_kelp", MissionType.GATHER, MissionDifficulty.EASY, "Gather 64 Kelp", Material.KELP, 64));

        // MEDIUM GATHERING (15)
        pool.add(new MissionTemplate("g_gold_raw", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 32 Raw Gold", Material.RAW_GOLD, 32));
        pool.add(new MissionTemplate("g_lapis", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 48 Lapis Lazuli", Material.LAPIS_LAZULI, 48));
        pool.add(new MissionTemplate("g_redstone", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 64 Redstone Dust", Material.REDSTONE, 64));
        pool.add(new MissionTemplate("g_obsidian", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 32 Obsidian", Material.OBSIDIAN, 32));
        pool.add(new MissionTemplate("g_amethyst", MissionType.GATHER, MissionDifficulty.MEDIUM, "Gather 16 Amethyst Shards", Material.AMETHYST_SHARD, 16));
        pool.add(new MissionTemplate("g_netherrack", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 256 Netherrack", Material.NETHERRACK, 256));
        pool.add(new MissionTemplate("g_soul_sand", MissionType.GATHER, MissionDifficulty.MEDIUM, "Gather 48 Soul Sand", Material.SOUL_SAND, 48));
        pool.add(new MissionTemplate("g_glowstone", MissionType.GATHER, MissionDifficulty.MEDIUM, "Gather 32 Glowstone Dust", Material.GLOWSTONE_DUST, 32));
        pool.add(new MissionTemplate("g_quartz", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 64 Nether Quartz", Material.QUARTZ, 64));
        pool.add(new MissionTemplate("g_magma", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 32 Magma Blocks", Material.MAGMA_BLOCK, 32));
        pool.add(new MissionTemplate("g_crying_obsidian", MissionType.GATHER, MissionDifficulty.MEDIUM, "Obtain 16 Crying Obsidian", Material.CRYING_OBSIDIAN, 16));
        pool.add(new MissionTemplate("g_basalt", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 64 Basalt", Material.BASALT, 64));
        pool.add(new MissionTemplate("g_blackstone", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 64 Blackstone", Material.BLACKSTONE, 64));
        pool.add(new MissionTemplate("g_deepslate", MissionType.GATHER, MissionDifficulty.MEDIUM, "Mine 128 Cobbled Deepslate", Material.COBBLED_DEEPSLATE, 128));
        pool.add(new MissionTemplate("g_dripstone", MissionType.GATHER, MissionDifficulty.MEDIUM, "Gather 32 Pointed Dripstone", Material.POINTED_DRIPSTONE, 32));

        // HARD GATHERING (15)
        pool.add(new MissionTemplate("g_diamonds", MissionType.GATHER, MissionDifficulty.HARD, "Mine 12 Diamonds", Material.DIAMOND, 12));
        pool.add(new MissionTemplate("g_ancient_debris", MissionType.GATHER, MissionDifficulty.HARD, "Mine 4 Ancient Debris", Material.ANCIENT_DEBRIS, 4));
        pool.add(new MissionTemplate("g_emerald", MissionType.GATHER, MissionDifficulty.HARD, "Mine 8 Emeralds", Material.EMERALD, 8));
        pool.add(new MissionTemplate("g_ghast_tear", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 2 Ghast Tears", Material.GHAST_TEAR, 2));
        pool.add(new MissionTemplate("g_phantom_membrane", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 5 Phantom Membranes", Material.PHANTOM_MEMBRANE, 5));
        pool.add(new MissionTemplate("g_slime_ball", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 16 Slimeballs", Material.SLIME_BALL, 16));
        pool.add(new MissionTemplate("g_sponge", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 2 Wet Sponges", Material.WET_SPONGE, 2));
        pool.add(new MissionTemplate("g_shulker_shell", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 2 Shulker Shells", Material.SHULKER_SHELL, 2));
        pool.add(new MissionTemplate("g_dragon_breath", MissionType.GATHER, MissionDifficulty.HARD, "Collect 3 Dragon's Breath", Material.DRAGON_BREATH, 3));
        pool.add(new MissionTemplate("g_nautilus", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 2 Nautilus Shells", Material.NAUTILUS_SHELL, 2));
        pool.add(new MissionTemplate("g_heart_sea", MissionType.GATHER, MissionDifficulty.HARD, "Find a Heart of the Sea", Material.HEART_OF_THE_SEA, 1));
        pool.add(new MissionTemplate("g_prismarine_shard", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 32 Prismarine Shards", Material.PRISMARINE_SHARD, 32));
        pool.add(new MissionTemplate("g_prismarine_crystals", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 16 Prismarine Crystals", Material.PRISMARINE_CRYSTALS, 16));
        pool.add(new MissionTemplate("g_wither_rose", MissionType.GATHER, MissionDifficulty.HARD, "Obtain 1 Wither Rose", Material.WITHER_ROSE, 1));
        pool.add(new MissionTemplate("g_echo_shard", MissionType.GATHER, MissionDifficulty.HARD, "Find 2 Echo Shards", Material.ECHO_SHARD, 2));

        // EASY CRAFTING (12)
        pool.add(new MissionTemplate("c_chests", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 16 Chests", Material.CHEST, 16));
        pool.add(new MissionTemplate("c_furnaces", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 8 Furnaces", Material.FURNACE, 8));
        pool.add(new MissionTemplate("c_torches", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 64 Torches", Material.TORCH, 64));
        pool.add(new MissionTemplate("c_bread", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 16 Bread", Material.BREAD, 16));
        pool.add(new MissionTemplate("c_paper", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 32 Paper", Material.PAPER, 32));
        pool.add(new MissionTemplate("c_glass", MissionType.CRAFT, MissionDifficulty.EASY, "Smelt 32 Glass", Material.GLASS, 32));
        pool.add(new MissionTemplate("c_ladders", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 32 Ladders", Material.LADDER, 32));
        pool.add(new MissionTemplate("c_fences", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 32 Oak Fences", Material.OAK_FENCE, 32));
        pool.add(new MissionTemplate("c_slabs", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 64 Stone Slabs", Material.STONE_SLAB, 64));
        pool.add(new MissionTemplate("c_stairs", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 32 Cobblestone Stairs", Material.COBBLESTONE_STAIRS, 32));
        pool.add(new MissionTemplate("c_signs", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 12 Oak Signs", Material.OAK_SIGN, 12));
        pool.add(new MissionTemplate("c_barrels", MissionType.CRAFT, MissionDifficulty.EASY, "Craft 8 Barrels", Material.BARREL, 8));

        // MEDIUM CRAFTING (12)
        pool.add(new MissionTemplate("c_tnt", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 24 TNT", Material.TNT, 24));
        pool.add(new MissionTemplate("c_bookshelves", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 12 Bookshelves", Material.BOOKSHELF, 12));
        pool.add(new MissionTemplate("c_piston", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 8 Pistons", Material.PISTON, 8));
        pool.add(new MissionTemplate("c_sticky_piston", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 4 Sticky Pistons", Material.STICKY_PISTON, 4));
        pool.add(new MissionTemplate("c_hopper", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 6 Hoppers", Material.HOPPER, 6));
        pool.add(new MissionTemplate("c_dispenser", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 4 Dispensers", Material.DISPENSER, 4));
        pool.add(new MissionTemplate("c_repeater", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 12 Redstone Repeaters", Material.REPEATER, 12));
        pool.add(new MissionTemplate("c_comparator", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 6 Redstone Comparators", Material.COMPARATOR, 6));
        pool.add(new MissionTemplate("c_observer", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 6 Observers", Material.OBSERVER, 6));
        pool.add(new MissionTemplate("c_iron_blocks", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 8 Blocks of Iron", Material.IRON_BLOCK, 8));
        pool.add(new MissionTemplate("c_gold_blocks", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 4 Blocks of Gold", Material.GOLD_BLOCK, 4));
        pool.add(new MissionTemplate("c_anvil", MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft 2 Anvils", Material.ANVIL, 2));

        // HARD CRAFTING (11)
        pool.add(new MissionTemplate("c_gpples", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 6 Golden Apples", Material.GOLDEN_APPLE, 6));
        pool.add(new MissionTemplate("c_ender_eye", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 12 Eyes of Ender", Material.ENDER_EYE, 12));
        pool.add(new MissionTemplate("c_diamond_block", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 2 Blocks of Diamond", Material.DIAMOND_BLOCK, 2));
        pool.add(new MissionTemplate("c_enchanting_table", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 1 Enchanting Table", Material.ENCHANTING_TABLE, 1));
        pool.add(new MissionTemplate("c_ender_chest", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 2 Ender Chests", Material.ENDER_CHEST, 2));
        pool.add(new MissionTemplate("c_respawn_anchor", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 1 Respawn Anchor", Material.RESPAWN_ANCHOR, 1));
        pool.add(new MissionTemplate("c_conduit", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 1 Conduit", Material.CONDUIT, 1));
        pool.add(new MissionTemplate("c_beacon", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 1 Beacon", Material.BEACON, 1));
        pool.add(new MissionTemplate("c_spectral_arrow", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 32 Spectral Arrows", Material.SPECTRAL_ARROW, 32));
        pool.add(new MissionTemplate("c_firework_rocket", MissionType.CRAFT, MissionDifficulty.HARD, "Craft 64 Firework Rockets", Material.FIREWORK_ROCKET, 64));
        pool.add(new MissionTemplate("c_shulker_box", MissionType.CRAFT, MissionDifficulty.HARD, "