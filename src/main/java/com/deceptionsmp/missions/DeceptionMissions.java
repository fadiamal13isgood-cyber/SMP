package com.deceptionsmp.missions;

import com.deceptionsmp.missions.command.BountyCommand;
import com.deceptionsmp.missions.listener.BountyListener;
import com.deceptionsmp.missions.listener.MissionListener;
import com.deceptionsmp.missions.listener.PlayerJoinListener;
import com.deceptionsmp.missions.manager.BountyManager;
import com.deceptionsmp.missions.manager.MissionManager;
import com.deceptionsmp.missions.manager.RewardManager;
import com.deceptionsmp.missions.manager.StorageManager;
import com.deceptionsmp.missions.model.Mission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    private BountyManager bountyManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storageManager = new StorageManager(this);
        this.rewardManager = new RewardManager(this);
        this.missionManager = new MissionManager(this);
        this.bountyManager = new BountyManager(this);

        getServer().getPluginManager().registerEvents(new MissionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);

        if (getCommand("mission") != null) getCommand("mission").setExecutor(this);
        if (getCommand("bounty") != null) getCommand("bounty").setExecutor(new BountyCommand(this));

        missionManager.startDeadlineChecker();
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.saveAllData();
        }
        if (bountyManager != null) {
            bountyManager.saveBounties();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run mission commands.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("mission")) {
            Mission m = missionManager.getActiveMission(player.getUniqueId());
            if (m == null) {
                player.sendMessage(Component.text("Assigning your initial secret mission...", NamedTextColor.YELLOW));
                missionManager.assignRandomMission(player);
                return true;
            }
            player.sendMessage(Component.text("=== YOUR SECRET MISSION ===", NamedTextColor.DARK_RED));
            player.sendMessage(Component.text("Difficulty: ", NamedTextColor.GOLD).append(Component.text(m.getDifficulty().name(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Objective: ", NamedTextColor.YELLOW).append(Component.text(m.getDescription(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Progress: ", NamedTextColor.GREEN).append(Component.text(m.getProgress() + "/" + m.getRequiredAmount(), NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Reward: ", NamedTextColor.AQUA).append(Component.text("Materials, Buffs & Heart Chance", NamedTextColor.WHITE)));
            long remainingMillis = m.getDeadlineTimestamp() - System.currentTimeMillis();
            long minutesLeft = Math.max(0, remainingMillis / 60000);
            player.sendMessage(Component.text("Time Left: ", NamedTextColor.RED).append(Component.text(minutesLeft + " minutes", NamedTextColor.WHITE)));
            player.sendMessage(Component.text("==========================", NamedTextColor.DARK_RED));
        }
        return true;
    }

    public static DeceptionMissions getInstance() { return instance; }
    public StorageManager getStorageManager() { return storageManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public BountyManager getBountyManager() { return bountyManager; }
}
