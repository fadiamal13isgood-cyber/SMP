package com.deceptionsmp.missions;

import com.deceptionsmp.missions.listener.MissionListener;
import com.deceptionsmp.missions.listener.PlayerJoinListener;
import com.deceptionsmp.missions.manager.MissionManager;
import com.deceptionsmp.missions.manager.RewardManager;
import com.deceptionsmp.missions.manager.StorageManager;
import com.deceptionsmp.missions.model.Mission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DeceptionMissions extends JavaPlugin implements CommandExecutor {

    private static DeceptionMissions instance;
    private StorageManager storageManager;
    private MissionManager missionManager;
    private RewardManager rewardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storageManager = new StorageManager(this);
        this.rewardManager = new RewardManager(this);
        this.missionManager = new MissionManager(this);

        getServer().getPluginManager().registerEvents(new MissionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        if (getCommand("mission") != null) getCommand("mission").setExecutor(this);
        if (getCommand("intel") != null) getCommand("intel").setExecutor(this);
        if (getCommand("credits") != null) getCommand("credits").setExecutor(this);
        if (getCommand("tokens") != null) getCommand("tokens").setExecutor(this);

        missionManager.startDeadlineChecker();
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.saveAllData();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run mission commands.");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "mission" -> {
                Mission m = missionManager.getActiveMission(player.getUniqueId());
                if (m == null) {
                    player.sendMessage(ChatColor.YELLOW + "Assigning your initial secret mission...");
                    missionManager.assignRandomMission(player);
                    return true;
                }
                player.sendMessage(ChatColor.DARK_RED + "=== YOUR SECRET MISSION ===");
                player.sendMessage(ChatColor.GOLD + "Difficulty: " + ChatColor.WHITE + m.getDifficulty());
                player.sendMessage(ChatColor.YELLOW + "Objective: " + ChatColor.WHITE + m.getDescription());
                player.sendMessage(ChatColor.GREEN + "Progress: " + ChatColor.WHITE + m.getProgress() + "/" + m.getRequiredAmount());
                long remainingMillis = m.getDeadlineTimestamp() - System.currentTimeMillis();
                long minutesLeft = Math.max(0, remainingMillis / 60000);
                player.sendMessage(ChatColor.RED + "Time Left: " + ChatColor.WHITE + minutesLeft + " minutes");
                player.sendMessage(ChatColor.DARK_RED + "==========================");
            }
            case "intel" -> player.sendMessage(ChatColor.AQUA + "Intel Balance: " + storageManager.getIntel(player.getUniqueId()));
            case "credits" -> player.sendMessage(ChatColor.GOLD + "Credits Balance: " + storageManager.getCredits(player.getUniqueId()));
            case "tokens" -> player.sendMessage(ChatColor.LIGHT_PURPLE + "Classified Tokens: " + storageManager.getTokens(player.getUniqueId()));
        }
        return true;
    }

    public static DeceptionMissions getInstance() { return instance; }
    public StorageManager getStorageManager() { return storageManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public RewardManager getRewardManager() { return rewardManager; }
}
