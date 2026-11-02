package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
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

    // 1. Mine/Gather Blocks
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        checkMissionItem(player, event.getBlock().getType(), 1);
    }

    // 2. Pick up items from the ground
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack item = event.getItem().getItemStack();
        checkMissionItem(player, item.getType(), item.getAmount());
    }

    // 3. Looting containers (Chests/Barrels/Shulkers) or grabbing from Creative
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        InventoryType invType = event.getInventory().getType();
        if (invType == InventoryType.CHEST || invType == InventoryType.BARREL || 
            invType == InventoryType.SHULKER_BOX || invType == InventoryType.CREATIVE || 
            invType == InventoryType.PLAYER || invType == InventoryType.CRAFTING) {

            if (event.getRawSlot() >= event.getView().getTopInventory().getSize() || event.isShiftClick()) {
                checkMissionItem(player, clickedItem.getType(), clickedItem.getAmount());
            }
        }
    }

    // 4. Crafting Items
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;

        String itemMaterial = result.getType().name();
        int amountCrafted = result.getAmount();

        if (event.isShiftClick()) {
            amountCrafted *= getMaxCraftAmount(event);
        }

        plugin.getMissionManager().checkProgress(player, "CRAFT", amountCrafted, itemMaterial);
    }

    private int getMaxCraftAmount(CraftItemEvent event) {
        int lowest = 64;
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                lowest = Math.min(lowest, ingredient.getAmount());
            }
        }
        return Math.max(1, lowest);
    }

    // 5. Hunting / Killing mobs & collecting drops
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

    private void checkMissionItem(Player player, Material material, int amount) {
        Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());
        if (mission == null) return;

        String target = mission.getTargetMaterial();
        if (target != null && target.equalsIgnoreCase(material.name())) {
            plugin.getMissionManager().checkProgress(player, mission.getType(), amount, material.name());
        }
    }
}
