package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.command.BountyCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BountyListener implements Listener {

    private final DeceptionMissions plugin;

    public BountyListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (!BountyCommand.bountyTargets.containsKey(player.getUniqueId())) {
            return;
        }

        String targetName = BountyCommand.bountyTargets.remove(player.getUniqueId());
        Inventory inv = event.getInventory();

        int totalItems = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems += item.getAmount();
            }
        }

        if (totalItems > 0) {
            Bukkit.broadcast(Component.text("⚡ A new bounty has been placed on " + targetName + " by " + player.getName() + " containing " + totalItems + " items!", NamedTextColor.GOLD));
            player.sendMessage(Component.text("Bounty successfully set on " + targetName + "!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Bounty cancelled (no items placed).", NamedTextColor.RED));
        }
    }
}
