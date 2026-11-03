package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final DeceptionMissions plugin;
    public static final HashMap<UUID, String> bountyTargets = new HashMap<>();

    public BountyCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        // Usage: /bounty <username> set
        if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
            String targetName = args[0];
            Player target = Bukkit.getPlayerExact(targetName);

            if (target == null) {
                player.sendMessage(Component.text("Player '" + targetName + "' is offline or does not exist!", NamedTextColor.RED));
                return true;
            }

            Inventory bountyGui = Bukkit.createInventory(player, 27, Component.text("Bounty: " + target.getName()));
            bountyTargets.put(player.getUniqueId(), target.getName());

            player.openInventory(bountyGui);
            player.sendMessage(Component.text("Place items inside the GUI and close it to confirm the bounty!", NamedTextColor.GREEN));
            return true;
        }

        player.sendMessage(Component.text("Usage: /bounty <username> set", NamedTextColor.YELLOW));
        return true;
    }
}
