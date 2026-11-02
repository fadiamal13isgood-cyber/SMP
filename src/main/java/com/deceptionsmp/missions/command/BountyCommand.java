package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final DeceptionMissions plugin;

    // Shared map to track pending bounty target selections / confirmations
    public static final Map<UUID, UUID> pendingBountyTargets = new HashMap<>();

    public BountyCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Subcommand: /bounty check <username>
        if (args.length >= 2 && args[0].equalsIgnoreCase("check")) {
            String targetName = args[1];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(Component.text("[Bounty] Player '" + targetName + "' is not online or does not exist.", NamedTextColor.RED));
                return true;
            }

            // Safely displays preview without giving items
            player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));
            player.sendMessage(Component.text(" BOUNTY DETAILS: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(targetPlayer.getName(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text(" • Target Status: ", NamedTextColor.GRAY)
                    .append(Component.text("WANTED", NamedTextColor.RED, TextDecoration.BOLD)));
            player.sendMessage(Component.text(" • Reward Preview: ", NamedTextColor.GRAY)
                    .append(Component.text("3x Diamonds, 1x Golden Apple, +Heart Shard", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text(" • Info: ", NamedTextColor.GRAY)
                    .append(Component.text("Eliminate this target in combat to claim the reward automatically.", NamedTextColor.DARK_GRAY)));
            player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));

            return true;
        }

        // Default Help Menu
        player.sendMessage(Component.text("========================================", NamedTextColor.RED));
        player.sendMessage(Component.text(" BOUNTY SYSTEM: ", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(" • /bounty check <player> ", NamedTextColor.YELLOW)
                .append(Component.text("- View a player's active bounty", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(" • /bounty list ", NamedTextColor.YELLOW)
                .append(Component.text("- View all top active bounties", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("========================================", NamedTextColor.RED));

        return true;
    }
}
