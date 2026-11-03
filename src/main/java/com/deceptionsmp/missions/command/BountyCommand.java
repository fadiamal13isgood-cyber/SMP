package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final DeceptionMissions plugin;

    // Session for players setting a bounty inside the set GUI
    public static final HashMap<UUID, String> bountyTargets = new HashMap<>();

    // Target Username (lowercase) -> List of Bounty ItemStacks placed on them
    public static final Map<String, List<ItemStack>> activeBounties = new HashMap<>();

    // Unclaimed rewards buffer per killer UUID -> List of earned bounty ItemStacks
    public static final Map<UUID, List<ItemStack>> unclaimedBountyRewards = new HashMap<>();

    public BountyCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        // --- SUBCOMMAND: /bounty claim ---
        if (args.length == 1 && args[0].equalsIgnoreCase("claim")) {
            List<ItemStack> rewards = unclaimedBountyRewards.get(player.getUniqueId());

            if (rewards == null || rewards.isEmpty()) {
                player.sendMessage(Component.text("You have no pending bounty rewards to claim!", NamedTextColor.RED));
                return true;
            }

            // Create GUI containing all unclaimed rewards across multiple kills
            Inventory claimGui = Bukkit.createInventory(player, 54, Component.text("Your Unclaimed Bounties"));
            for (int i = 0; i < Math.min(rewards.size(), 54); i++) {
                ItemStack item = rewards.get(i);
                if (item != null && item.getType() != Material.AIR) {
                    claimGui.setItem(i, item);
                }
            }

            player.openInventory(claimGui);
            player.sendMessage(Component.text("Take your bounty rewards out of the GUI!", NamedTextColor.GREEN));
            return true;
        }

        if (args.length < 2) {
            sendHelp(player);
            return true;
        }

        String targetName = args[0];
        String action = args[1];

        // --- SUBCOMMAND: /bounty <username> set ---
        if (action.equalsIgnoreCase("set")) {
            Player target = Bukkit.getPlayerExact(targetName);

            if (target == null) {
                player.sendMessage(Component.text("Player '" + targetName + "' is offline or does not exist!", NamedTextColor.RED));
                return true;
            }

            Inventory bountyGui = Bukkit.createInventory(player, 27, Component.text("Bounty: " + target.getName()));
            bountyTargets.put(player.getUniqueId(), target.getName());

            player.openInventory(bountyGui);
            player.sendMessage(Component.text("Place items inside the GUI and close it to set the bounty!", NamedTextColor.GREEN));
            return true;
        }

        // --- SUBCOMMAND: /bounty <username> check ---
        if (action.equalsIgnoreCase("check")) {
            List<ItemStack> bountyItems = activeBounties.get(targetName.toLowerCase());

            if (bountyItems == null || bountyItems.isEmpty()) {
                player.sendMessage(Component.text("There is currently no active bounty on " + targetName + ".", NamedTextColor.YELLOW));
                return true;
            }

            Inventory checkGui = Bukkit.createInventory(null, 27, Component.text("Bounty Rewards: " + targetName));

            int slot = 0;
            for (ItemStack item : bountyItems) {
                if (slot >= 27) break;
                if (item != null && item.getType() != Material.AIR) {
                    checkGui.setItem(slot++, item.clone());
                }
            }

            player.openInventory(checkGui);
            player.sendMessage(Component.text("Viewing bounty items for " + targetName + ".", NamedTextColor.AQUA));
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("--- Bounty Help ---", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("/bounty <username> set ", NamedTextColor.YELLOW)
                .append(Component.text("- Open GUI to place a bounty on someone", NamedTextColor.WHITE)));
        player.sendMessage(Component.text("/bounty <username> check ", NamedTextColor.YELLOW)
                .append(Component.text("- View current bounty on someone", NamedTextColor.WHITE)));
        player.sendMessage(Component.text("/bounty claim ", NamedTextColor.YELLOW)
                .append(Component.text("- Open GUI to claim all your earned bounty rewards", NamedTextColor.WHITE)));
    }
}
