package com.deceptionsmp.missions.manager;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.MissionDifficulty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RewardManager {

    private final DeceptionMissions plugin;

    public RewardManager(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    public void giveRewards(Player player, MissionDifficulty diff) {
        player.sendMessage(Component.text("+" + diff.getXpReward() + " XP Rank Progress", NamedTextColor.GOLD));
    }

    public void applyPunishment(Player player, MissionDifficulty diff) {
        int durationTicks = (diff.ordinal() + 1) * 1200;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 0));
    }
}
