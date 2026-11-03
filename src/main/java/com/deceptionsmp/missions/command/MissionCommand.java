package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MissionCommand implements CommandExecutor {

    private final DeceptionMissions plugin;

    public MissionCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Mission active = plugin.getMissionManager().getActiveMission(player.getUniqueId());

        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));
        player.sendMessage(Component.text(" SECRET MISSION STATUS ", NamedTextColor.RED, TextDecoration.BOLD));

        if (active == null) {
            player.sendMessage(Component.text(" • You currently have no active secret mission.", NamedTextColor.GRAY));
            player.sendMessage(Component.text(" • Re-join or wait for the system to assign one.", NamedTextColor.DARK_GRAY));
        } else {
            long remainingSec = Math.max(0, (active.getDeadlineTimestamp() - System.currentTimeMillis()) / 1000);
            long minutes = remainingSec / 60;
            long seconds = remainingSec % 60;

            player.sendMessage(Component.text(" • Objective: ", NamedTextColor.GRAY)
                    .append(Component.text(active.getDescription(), NamedTextColor.GOLD, TextDecoration.BOLD)));
            player.sendMessage(Component.text(" • Difficulty: ", NamedTextColor.GRAY)
                    .append(Component.text(active.getDifficulty().name(), NamedTextColor.YELLOW)));
            player.sendMessage(Component.text(" • Progress: ", NamedTextColor.GRAY)
                    .append(Component.text(active.getProgress() + " / " + active.getRequiredAmount(), NamedTextColor.AQUA)));
            player.sendMessage(Component.text(" • Time Remaining: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%dm %ds", minutes, seconds), NamedTextColor.GREEN)));
            player.sendMessage(Component.text(" • Potential Rewards: ", NamedTextColor.GRAY)
                    .append(Component.text("Ingots/Diamonds, Potion Buffs & 0.5% Max Heart Chance", NamedTextColor.LIGHT_PURPLE)));
        }

        player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));
        return true;
    }
}
