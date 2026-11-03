package com.deceptionsmp.missions.manager; // or your command package

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    // Tracks which player is setting a bounty on which target UUID inside which inventory
    public static final HashMap<UUID, String> bountyTargets = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Usage: /bounty <username> set
        if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
            String targetName = args[0];
            Player target = Bukkit.getPlayerExact(targetName);

            // You can set a bounty on yourself or anyone else online
            if (target == null) {
                player.sendMessage(Component.text("Player not found or offline!", NamedTextColor.RED));
                return true;
            }

            // Create a 27-slot chest GUI
            Inventory bountyGui = Bukkit.createInventory(player, 27, Component.text("Bounty: " + target.getName()));
            
            // Save state mapping
            bountyTargets.put(player.getUniqueId(), target.getName());

            player.openInventory(bountyGui);
            player.sendMessage(Component.text("Place items inside the GUI to fund the bounty. Close it to confirm!", NamedTextColor.GREEN));
            return true;
        }

        player.sendMessage(Component.text("Usage: /bounty <username> set", NamedTextColor.YELLOW));
        return true;
    }
}
