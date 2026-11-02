package com.deceptionsmp.missions.listener;

import com.deceptionsmp.missions.DeceptionMissions;
import com.deceptionsmp.missions.model.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final DeceptionMissions plugin;

    public PlayerJoinListener(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Mission active = plugin.getMissionManager().getActiveMission(player.getUniqueId());

        if (active == null) {
            // Only assign a fresh mission if they don't have an active one
            plugin.getMissionManager().assignRandomMission(player);
        } else {
            // Keep existing mission and notify player their progress is safe
            player.sendMessage("§a[Missions] Welcome back! Your active mission (" + active.getDescription() + ") is still active.");
        }
    }
}
