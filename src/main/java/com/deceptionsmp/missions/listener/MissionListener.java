package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class MissionListener implements Listener {

    private final DeceptionMissions plugin;

    public MissionListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockMaterial = event.getBlock().getType().name();
        
        plugin.getMissionManager().checkProgress(player, "GATHER", 1, blockMaterial);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (result == null) return;

        String itemMaterial = result.getType().name();
        int amountCrafted = result.getAmount();

        plugin.getMissionManager().checkProgress(player, "CRAFT", amountCrafted, itemMaterial);
    }

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
