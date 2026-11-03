package com.deceptionsmp.missions.manager;

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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();

        // Check if this inventory is a Bounty GUI using Adventure Component title check or tracking map
        if (!BountyCommand.bountyTargets.containsKey(player.getUniqueId())) {
            return;
        }

        String targetName = BountyCommand.bountyTargets.remove(player.getUniqueId());
        Inventory inv = event.getInventory();

        int totalItems = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalItems += item.getAmount();
                // TODO: Add your internal bounty storage / logic here with 'targetName' and 'item'
            }
        }

        if (totalItems > 0) {
            Bukkit.broadcast(Component.text("⚡ A new bounty has been placed on " + targetName + " by " + player.getName() + " worth " + totalItems + " items!", NamedTextColor.GOLD));
            player.sendMessage(Component.text("Bounty successfully created!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Bounty creation cancelled (no items placed).", NamedTextColor.RED));
        }
    }
}
