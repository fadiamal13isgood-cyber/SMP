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

    // Map to keep track of pending targets for BountyListener
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

        // Subcommand 1: /bounty claim <username>
        if (args.length >= 2 && args[0].equalsIgnoreCase("claim")) {
            String targetName = args[1];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(Component.text("[Bounty] Player '" + targetName + "' is not online or available.", NamedTextColor.RED));
                return true;
            }

            // Verify if player has an active bounty contract/kill on the target
            UUID trackedTarget = pendingBountyTargets.get(player.getUniqueId());
            if (trackedTarget != null && trackedTarget.equals(targetPlayer.getUniqueId())) {
                pendingBountyTargets.remove(player.getUniqueId());

                player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));
                player.sendMessage(Component.text(" BOUNTY CLAIMED: ", NamedTextColor.GREEN, TextDecoration.BOLD)
                        .append(Component.text(targetPlayer.getName(), NamedTextColor.WHITE)));
                player.sendMessage(Component.text(" • Contract Status: ", NamedTextColor.GRAY)
                        .append(Component.text("SUCCESSFUL", NamedTextColor.GREEN, TextDecoration.BOLD)));
                player.sendMessage(Component.text(" • Rewards delivered to your inventory!", NamedTextColor.YELLOW));
                player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));
            } else {
                player.sendMessage(Component.text("[Bounty] You do not have an active claimable bounty contract for " + targetPlayer.getName() + "!", NamedTextColor.RED));
            }

            return true;
        }

        // Subcommand 2: /bounty check <username>
        if (args.length >= 2 && args[0].equalsIgnoreCase("check")) {
            String targetName = args[1];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(Component.text("[Bounty] Player '" + targetName + "' is not online or does not exist.", NamedTextColor.RED));
                return true;
            }

            player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));
            player.sendMessage(Component.text(" BOUNTY DETAILS: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .append(Component.text(targetPlayer.getName(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text(" • Target Status: ", NamedTextColor.GRAY)
                    .append(Component.text("WANTED", NamedTextColor.RED, TextDecoration.BOLD)));
            player.sendMessage(Component.text(" • Reward Preview: ", NamedTextColor.GRAY)
                    .append(Component.text("Diamonds, Golden Apples, Custom Buffs & 0.5% Max Heart Chance", NamedTextColor.YELLOW)));
            player.sendMessage(Component.text(" • Info: ", NamedTextColor.GRAY)
                    .append(Component.text("Eliminate this target in combat to claim rewards automatically.", NamedTextColor.DARK_GRAY)));
            player.sendMessage(Component.text("========================================", NamedTextColor.DARK_RED));

            return true;
        }

        // Subcommand 3: /bounty <username> (Set target)
        if (args.length >= 1) {
            String targetName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(Component.text("[Bounty] Player '" + targetName + "' is not online or does not exist.", NamedTextColor.RED));
                return true;
            }

            pendingBountyTargets.put(player.getUniqueId(), targetPlayer.getUniqueId());

            player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));
            player.sendMessage(Component.text(" BOUNTY TARGET SELECTED: ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .append(Component.text(targetPlayer.getName(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text(" • Status: ", NamedTextColor.GRAY)
                    .append(Component.text("Ready to initialize contract.", NamedTextColor.GREEN)));
            player.sendMessage(Component.text("========================================", NamedTextColor.GOLD));

            return true;
        }

        // Default Help Menu
        player.sendMessage(Component.text("========================================", NamedTextColor.RED));
        player.sendMessage(Component.text(" BOUNTY SYSTEM: ", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(" • /bounty <player> ", NamedTextColor.YELLOW)
                .append(Component.text("- Set or target a bounty", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(" • /bounty claim <player> ", NamedTextColor.YELLOW)
                .append(Component.text("- Claim a completed bounty reward", NamedTextColor.GRAY)));
        player.sendMessage(Component.text(" • /bounty check <player> ", NamedTextColor.YELLOW)
                .append(Component.text("- View player bounty details", NamedTextColor.GRAY)));
        player.sendMessage(Component.text("========================================", NamedTextColor.RED));

        return true;
    }
}
