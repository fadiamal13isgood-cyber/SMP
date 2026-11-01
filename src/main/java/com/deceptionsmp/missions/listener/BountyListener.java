package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.command.BountyCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BountyListener implements Listener {

    private final DeceptionMissions plugin;

    public BountyListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String title = event.getView().getTitle();

        if (title.startsWith("Place Bounty: ")) {
            UUID targetUuid = BountyCommand.pendingBountyTargets.remove(player.getUniqueId());
            if (targetUuid == null) return;

            Player target = Bukkit.getPlayer(targetUuid);
            List<ItemStack> items = new ArrayList<>();

            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null) items.add(item);
            }

            if (items.isEmpty()) {
                player.sendMessage(Component.text("Bounty cancelled: No items placed.", NamedTextColor.RED));
                return;
            }

            plugin.getBountyManager().addBounty(targetUuid, items);
            String targetName = (target != null) ? target.getName() : "a player";

            Bukkit.broadcast(Component.text("[BOUNTY] ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                    .append(Component.text(player.getName() + " placed a bounty on " + targetName + "!", NamedTextColor.YELLOW)));
        } else if (title.equals("Claim Bounty Rewards")) {
            plugin.getBountyManager().saveClaimGui(player, event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            if (plugin.getBountyManager().hasBounty(victim.getUniqueId())) {
                plugin.getBountyManager().handleKill(killer, victim);

                Bukkit.broadcast(Component.text("[BOUNTY] ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                        .append(Component.text(killer.getName() + " eliminated " + victim.getName() + " and claimed their bounty!", NamedTextColor.GOLD)));

                killer.sendMessage(Component.text("Type /bounty claim to open your rewards menu!", NamedTextColor.GREEN, TextDecoration.BOLD));
            }
        }
    }
}
