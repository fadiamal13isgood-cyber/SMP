package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class MissionListener implements Listener {

    private final DeceptionMissions plugin;

    public MissionListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    // 1. GATHER / MINE MISSIONS (Blocks broken)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockMaterial = event.getBlock().getType().name();
        plugin.getMissionManager().checkProgress(player, "GATHER", 1, blockMaterial);
    }

    // 2. PICK UP ITEMS (Ground / Loot)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        plugin.getMissionManager().checkProgress(player, "GATHER", item.getAmount(), item.getType().name());
    }

    // 3. CHEST LOOTING & CREATIVE MODE SPAWNS (Inventory clicks / taking from containers)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Check if player is interacting with a chest, barrel, shulker box, player inventory, or creative menu
        InventoryType invType = event.getInventory().getType();
        if (invType == InventoryType.CHEST || invType == InventoryType.BARREL || 
            invType == InventoryType.SHULKER_BOX || invType == InventoryType.CREATIVE || 
            invType == InventoryType.PLAYER || invType == InventoryType.CRAFTING) {
            
            ItemStack clickedItem = event.getCurrentItem();
            // If they are moving an item into their inventory from a chest/creative menu
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                // Only count when moving items into player's main inventory / hotbar slots
                if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER &&
                    event.isShiftClick() || (event.getRawSlot() >= event.getView().getTopInventory().getSize())) {
                    
                    plugin.getMissionManager().checkProgress(player, "GATHER", clickedItem.getAmount(), clickedItem.getType().name());
                }
            }
        }
    }

    // 4. CRAFT MISSIONS
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;

        String itemMaterial = result.getType().name();
        int amountCrafted = result.getAmount();

        // Handle shift-clicking full recipe results
        if (event.isShiftClick()) {
            int maxPossible = getMaxCraftAmount(event);
            amountCrafted *= maxPossible;
        }

        plugin.getMissionManager().checkProgress(player, "CRAFT", amountCrafted, itemMaterial);
    }

    // Helper utility to calculate shift-click bulk crafting amounts
    private int getMaxCraftAmount(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        int resultingAmount = result.getAmount();
        int lowest = 64;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                lowest = Math.min(lowest, ingredient.getAmount());
            }
        }
        return Math.max(1, lowest);
    }

    // 5. HUNT / KILL MISSIONS
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        for (ItemStack drop : event.getDrops()) {
            if (drop != null) {
                plugin.getMissionManager().checkProgress(killer, "HUNT", drop.getAmount(), drop.getType().name());
            }
        }
        
        plugin.getMissionManager().checkProgress(killer, "HUNT", 1, event.getEntityType().name());
    }
}
