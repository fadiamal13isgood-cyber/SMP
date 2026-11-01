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
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Mission mission = plugin.getMissionManager().getActiveMission(player.getUniqueId());

        if (mission == null) {
            plugin.getMissionManager().assignRandomMission(player);
        } else if (System.currentTimeMillis() >= mission.getDeadlineTimestamp()) {
            plugin.getMissionManager().failMission(player.getUniqueId(), mission);
        }
    }
}
