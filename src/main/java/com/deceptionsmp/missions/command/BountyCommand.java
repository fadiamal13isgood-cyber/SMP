package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final DeceptionMissions plugin;
    public static final Map<UUID, UUID> pendingBountyTargets = new HashMap<>();

    public BountyCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /bounty.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("claim")) {
            plugin.getBountyManager().openClaimGui(player);
            return true;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage("§cPlayer not found or offline.");
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cYou cannot set a bounty on yourself!");
                return true;
            }

            pendingBountyTargets.put(player.getUniqueId(), target.getUniqueId());
            Inventory gui = Bukkit.createInventory(null, 27, "Place Bounty: " + target.getName());
            player.openInventory(gui);
            return true;
        }

        player.sendMessage("§cUsage: /bounty <player> OR /bounty claim");
        return true;
    }
}
