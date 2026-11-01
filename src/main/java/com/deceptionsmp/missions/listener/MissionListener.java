package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MissionListener implements Listener {

    private final DeceptionMissions plugin;
    private final Map<UUID, Long> killCooldowns = new HashMap<>();

    public MissionListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        UUID killerId = killer.getUniqueId();
        long now = System.currentTimeMillis();

        if (killCooldowns.containsKey(killerId) && (now - killCooldowns.get(killerId) < 300000)) {
            return; // Anti-farming: 5-minute cooldown between counted kills
        }

        killCooldowns.put(killerId, now);
        plugin.getMissionManager().checkProgress(killer, "KILL_PLAYERS", 1, "");
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String mat = event.getItem().getItemStack().getType().name();
        int amount = event.getItem().getItemStack().getAmount();
        plugin.getMissionManager().checkProgress(player, "COLLECT_ITEMS", amount, mat);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        Player player = event.getPlayer();
        Location spawn = player.getWorld().getSpawnLocation();
        int dist = (int) player.getLocation().distance(spawn);
        plugin.getMissionManager().checkProgress(player, "EXPLORE", dist, "");
    }
}
