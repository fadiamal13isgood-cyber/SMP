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

        Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());
        if (mission == null) {
            player.sendMessage(Component.text("[Missions] You do not have an active mission right now!", NamedTextColor.RED));
            return true;
        }

        long timeLeftMs = mission.getDeadlineTimestamp() - System.currentTimeMillis();
        long minutesLeft = Math.max(0, timeLeftMs / 60000);

        player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));
        player.sendMessage(Component.text(" ACTIVE MISSION: ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .append(Component.text(mission.getDescription(), NamedTextColor.WHITE)));
        player.sendMessage(Component.text(" • Type: ", NamedTextColor.GRAY)
                .append(Component.text(mission.getType(), NamedTextColor.AQUA)));
        player.sendMessage(Component.text(" • Difficulty: ", NamedTextColor.GRAY)
                .append(Component.text(mission.getDifficulty().name(), NamedTextColor.LIGHT_PURPLE)));
        player.sendMessage(Component.text(" • Progress: ", NamedTextColor.GRAY)
                .append(Component.text(mission.getProgress() + " / " + mission.getRequiredAmount(), NamedTextColor.GREEN)));
        player.sendMessage(Component.text(" • Time Remaining: ", NamedTextColor.GRAY)
                .append(Component.text(minutesLeft + " minutes", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));

        return true;
    }
}
