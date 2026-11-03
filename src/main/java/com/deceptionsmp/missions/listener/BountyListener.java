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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BountyListener implements Listener {

    private final DeceptionMissions plugin;

    public BountyListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        List<ItemStack> bounty = BountyCommand.activeBounties.remove(victim.getName().toLowerCase());

        if (bounty != null && !bounty.isEmpty()) {
            List<ItemStack> killerRewards = BountyCommand.unclaimedBountyRewards.computeIfAbsent(
                    killer.getUniqueId(), k -> new ArrayList<>()
            );
            killerRewards.addAll(bounty);

            Bukkit.broadcast(Component.text("🎯 " + killer.getName() + " killed " + victim.getName() + " and claimed their bounty! Use /bounty claim to collect your rewards.", NamedTextColor.GOLD));
            
            // Save state change to file
            BountyCommand.saveBountyData(plugin);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (event.getView().getTitle().equals("Your Unclaimed Bounties")) {
            List<ItemStack> remaining = new ArrayList<>();
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    remaining.add(item.clone());
                }
            }
            if (remaining.isEmpty()) {
                BountyCommand.unclaimedBountyRewards.remove(player.getUniqueId());
            } else {
                BountyCommand.unclaimedBountyRewards.put(player.getUniqueId(), remaining);
            }
            
            // Save state change to file
            BountyCommand.saveBountyData(plugin);
            return;
        }

        if (!BountyCommand.bountyTargets.containsKey(player.getUniqueId())) {
            return;
        }

        String targetName = BountyCommand.bountyTargets.remove(player.getUniqueId());
        Inventory inv = event.getInventory();

        List<ItemStack> placedItems = new ArrayList<>();
        int totalItemsCount = 0;

        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                placedItems.add(item.clone());
                totalItemsCount += item.getAmount();
            }
        }

        if (!placedItems.isEmpty()) {
            List<ItemStack> existingBounty = BountyCommand.activeBounties.computeIfAbsent(
                    targetName.toLowerCase(), k -> new ArrayList<>()
            );
            existingBounty.addAll(placedItems);

            Bukkit.broadcast(Component.text("⚡ A new bounty has been placed on " + targetName + " by " + player.getName() + " containing " + totalItemsCount + " items!", NamedTextColor.GOLD));
            player.sendMessage(Component.text("Bounty successfully set on " + targetName + "!", NamedTextColor.GREEN));
            
            // Save state change to file
            BountyCommand.saveBountyData(plugin);
        } else {
            player.sendMessage(Component.text("Bounty cancelled (no items placed).", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Bounty Rewards:")) {
            event.setCancelled(true);
        }
    }
}
