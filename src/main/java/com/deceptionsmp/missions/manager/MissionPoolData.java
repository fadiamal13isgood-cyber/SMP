package com.deceptionsmp.missions.manager;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class MissionPoolData {

    public enum MissionType {
        GATHER, CRAFT, HUNT, HUNT_PLAYER
    }

    public enum MissionDifficulty {
        EASY, MEDIUM, HARD
    }

    public static List<MissionTemplate> getPool() {
        List<MissionTemplate> pool = new ArrayList<>();

        // --- GATHERING QUESTS ---
        Material[] easyGather = {Material.COBBLESTONE, Material.COAL, Material.RAW_IRON, Material.RAW_COPPER, Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.SAND, Material.GRAVEL, Material.DIRT, Material.WHEAT, Material.CARROT, Material.POTATO, Material.SUGAR_CANE, Material.KELP, Material.CLAY_BALL, Material.BAMBOO, Material.CACTUS, Material.PUMPKIN, Material.MELON_SLICE};
        for (int i = 0; i < easyGather.length; i++) {
            pool.add(new MissionTemplate("g_easy_" + i, MissionType.GATHER, MissionDifficulty.EASY, "Gather " + easyGather[i].name(), easyGather[i], 32 + (i * 2)));
        }

        Material[] medGather = {Material.RAW_GOLD, Material.LAPIS_LAZULI, Material.REDSTONE, Material.OBSIDIAN, Material.AMETHYST_SHARD, Material.NETHERRACK, Material.SOUL_SAND, Material.GLOWSTONE_DUST, Material.QUARTZ, Material.MAGMA_BLOCK, Material.CRYING_OBSIDIAN, Material.BASALT, Material.BLACKSTONE, Material.COBBLED_DEEPSLATE, Material.POINTED_DRIPSTONE, Material.MOSS_BLOCK, Material.PRISMARINE_SHARD, Material.NETHER_BRICKS, Material.SNOW_BLOCK, Material.HONEY_BLOCK};
        for (int i = 0; i < medGather.length; i++) {
            pool.add(new MissionTemplate("g_med_" + i, MissionType.GATHER, MissionDifficulty.MEDIUM, "Gather " + medGather[i].name(), medGather[i], 16 + i));
        }

        Material[] hardGather = {Material.DIAMOND, Material.ANCIENT_DEBRIS, Material.EMERALD, Material.GHAST_TEAR, Material.PHANTOM_MEMBRANE, Material.SLIME_BALL, Material.WET_SPONGE, Material.SHULKER_SHELL, Material.DRAGON_BREATH, Material.NAUTILUS_SHELL, Material.HEART_OF_THE_SEA, Material.PRISMARINE_CRYSTALS, Material.WITHER_ROSE, Material.ECHO_SHARD, Material.TOTEM_OF_UNDYING};
        for (int i = 0; i < hardGather.length; i++) {
            pool.add(new MissionTemplate("g_hard_" + i, MissionType.GATHER, MissionDifficulty.HARD, "Gather " + hardGather[i].name(), hardGather[i], Math.max(1, i % 4)));
        }

        // --- CRAFTING QUESTS ---
        Material[] easyCraft = {Material.CHEST, Material.FURNACE, Material.TORCH, Material.BREAD, Material.PAPER, Material.GLASS, Material.LADDER, Material.OAK_FENCE, Material.STONE_SLAB, Material.COBBLESTONE_STAIRS, Material.OAK_SIGN, Material.BARREL, Material.BOW, Material.ARROW, Material.BUCKET};
        for (int i = 0; i < easyCraft.length; i++) {
            pool.add(new MissionTemplate("c_easy_" + i, MissionType.CRAFT, MissionDifficulty.EASY, "Craft " + easyCraft[i].name(), easyCraft[i], 8 + i));
        }

        Material[] medCraft = {Material.TNT, Material.BOOKSHELF, Material.PISTON, Material.STICKY_PISTON, Material.HOPPER, Material.DISPENSER, Material.REPEATER, Material.COMPARATOR, Material.OBSERVER, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.ANVIL, Material.CROSSBOW, Material.IRON_BARS, Material.SHIELD};
        for (int i = 0; i < medCraft.length; i++) {
            pool.add(new MissionTemplate("c_med_" + i, MissionType.CRAFT, MissionDifficulty.MEDIUM, "Craft " + medCraft[i].name(), medCraft[i], 2 + (i % 5)));
        }

        Material[] hardCraft = {Material.GOLDEN_APPLE, Material.ENDER_EYE, Material.DIAMOND_BLOCK, Material.ENCHANTING_TABLE, Material.ENDER_CHEST, Material.RESPAWN_ANCHOR, Material.CONDUIT, Material.BEACON, Material.SPECTRAL_ARROW, Material.FIREWORK_ROCKET};
        for (int i = 0; i < hardCraft.length; i++) {
            pool.add(new MissionTemplate("c_hard_" + i, MissionType.CRAFT, MissionDifficulty.HARD, "Craft " + hardCraft[i].name(), hardCraft[i], 1 + (i % 3)));
        }

        // --- HUNTING QUESTS ---
        Material[] easyHunt = {Material.ROTTEN_FLESH, Material.BONE, Material.STRING, Material.GUNPOWDER, Material.LEATHER, Material.PORKCHOP, Material.FEATHER, Material.WHITE_WOOL, Material.INK_SAC, Material.COPPER_INGOT};
        for (int i = 0; i < easyHunt.length; i++) {
            pool.add(new MissionTemplate("h_easy_" + i, MissionType.HUNT, MissionDifficulty.EASY, "Hunt / Eliminate", easyHunt[i], 10));
        }

        Material[] medHunt = {Material.SPIDER_EYE, Material.ENDER_PEARL, Material.GLASS_BOTTLE, Material.GOLD_NUGGET, Material.MAGMA_CREAM, Material.SLIME_BALL, Material.PRISMARINE_SHARD, Material.CROSSBOW, Material.PHANTOM_MEMBRANE, Material.FEATHER};
        for (int i = 0; i < medHunt.length; i++) {
            pool.add(new MissionTemplate("h_med_" + i, MissionType.HUNT, MissionDifficulty.MEDIUM, "Hunt / Eliminate", medHunt[i], 5));
        }

        Material[] hardHunt = {Material.BLAZE_ROD, Material.WITHER_SKELETON_SKULL, Material.GHAST_TEAR, Material.SADDLE, Material.TOTEM_OF_UNDYING, Material.SPONGE, Material.GOLDEN_AXE, Material.SHULKER_SHELL, Material.BREEZE_ROD, Material.NETHER_STAR};
        for (int i = 0; i < hardHunt.length; i++) {
            pool.add(new MissionTemplate("h_hard_" + i, MissionType.HUNT, MissionDifficulty.HARD, "Eliminate Boss/Target", hardHunt[i], 1));
        }

        // --- PLAYER ASSASSINATION QUESTS ---
        pool.add(new MissionTemplate("hp_easy", MissionType.HUNT_PLAYER, MissionDifficulty.EASY, "Assassinate Target Player", null, 1));
        pool.add(new MissionTemplate("hp_medium", MissionType.HUNT_PLAYER, MissionDifficulty.MEDIUM, "Assassinate Target Player", null, 1));
        pool.add(new MissionTemplate("hp_hard", MissionType.HUNT_PLAYER, MissionDifficulty.HARD, "Assassinate Target Player", null, 1));

        return pool;
    }

    public record MissionTemplate(String id, MissionType type, MissionDifficulty difficulty, String description, Material targetMaterial, int requiredAmount) {}
}
