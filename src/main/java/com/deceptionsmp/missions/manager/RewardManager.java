package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.MissionDifficulty;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RewardManager {

    private final DeceptionMissions plugin;

    public RewardManager(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    public void giveRewards(Player player, MissionDifficulty diff) {
        StorageManager store = plugin.getStorageManager();
        switch (diff) {
            case EASY -> {
                store.addIntel(player.getUniqueId(), 10);
                store.addCredits(player.getUniqueId(), 5);
                player.sendMessage(ChatColor.GOLD + "+10 Intel | +5 Credits");
            }
            case MEDIUM -> {
                store.addIntel(player.getUniqueId(), 25);
                store.addCredits(player.getUniqueId(), 15);
                player.sendMessage(ChatColor.GOLD + "+25 Intel | +15 Credits");
            }
            case HARD -> {
                store.addIntel(player.getUniqueId(), 50);
                store.addCredits(player.getUniqueId(), 35);
                store.addTokens(player.getUniqueId(), 1);
                player.sendMessage(ChatColor.GOLD + "+50 Intel | +35 Credits | +1 Classified Token");
            }
            case CLASSIFIED -> {
                store.addIntel(player.getUniqueId(), 100);
                store.addCredits(player.getUniqueId(), 80);
                store.addTokens(player.getUniqueId(), 3);
                player.sendMessage(ChatColor.GOLD + "+100 Intel | +80 Credits | +3 Classified Tokens");
            }
        }
    }

    public void applyPunishment(Player player, MissionDifficulty diff) {
        int durationTicks = (diff.ordinal() + 1) * 1200;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 0));
        player.sendMessage(ChatColor.DARK_RED + "Debuff applied for mission failure!");
    }
}
