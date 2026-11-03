package com.deceptionsmp.missions.command;

import com.deceptionsmp.missions.DeceptionMissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BountyCommand implements CommandExecutor {

    private final DeceptionMissions plugin;

    public static final HashMap<UUID, String> bountyTargets = new HashMap<>();
    public static final Map<String, List<ItemStack>> activeBounties = new HashMap<>();
    public static final Map<UUID, List<ItemStack>> unclaimedBountyRewards = new HashMap<>();

    public BountyCommand(DeceptionMissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("claim")) {
            List<ItemStack> rewards = unclaimedBountyRewards.get(player.getUniqueId());

            if (rewards == null || rewards.isEmpty()) {
                player.sendMessage(Component.text("You have no pending bounty rewards to claim!", NamedTextColor.RED));
                return true;
            }

            Inventory claimGui = Bukkit.createInventory(player, 54, Component.text("Your Unclaimed Bounties"));
            for (int i = 0; i < Math.min(rewards.size(), 54); i++) {
                ItemStack item = rewards.get(i);
                if (item != null && item.getType() != Material.AIR) {
                    claimGui.setItem(i, item);
                }
            }

            player.openInventory(claimGui);
            player.sendMessage(Component.text("Take your bounty rewards out of the GUI!", NamedTextColor.GREEN));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            if (activeBounties.isEmpty()) {
                player.sendMessage(Component.text("There are currently no active bounties.", NamedTextColor.YELLOW));
                return true;
            }

            player.sendMessage(Component.text("--- Active Bounties ---", NamedTextColor.GOLD, TextDecoration.BOLD));
            int count = 1;
            for (Map.Entry<String, List<ItemStack>> entry : activeBounties.entrySet()) {
                if (count > 10) break;
                int totalAmount = 0;
                for (ItemStack item : entry.getValue()) {
                    if (item != null) totalAmount += item.getAmount();
                }
                player.sendMessage(Component.text("#" + count + " ", NamedTextColor.YELLOW)
                        .append(Component.text(entry.getKey(), NamedTextColor.WHITE))
                        .append(Component.text(" — Items: ", NamedTextColor.GRAY))
                        .append(Component.text(totalAmount, NamedTextColor.AQUA)));
                count++;
            }
            return true;
        }

        if (args.length < 2) {
            sendHelp(player);
            return true;
        }

        String targetName = args[0];
        String action = args[1];

        if (action.equalsIgnoreCase("set")) {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                player.sendMessage(Component.text("Player '" + targetName + "' is offline or does not exist!", NamedTextColor.RED));
                return true;
            }

            Inventory bountyGui = Bukkit.createInventory(player, 27, Component.text("Bounty: " + target.getName()));
            bountyTargets.put(player.getUniqueId(), target.getName());
            player.openInventory(bountyGui);
            player.sendMessage(Component.text("Place items inside the GUI and close it to set the bounty!", NamedTextColor.GREEN));
            return true;
        }

        if (action.equalsIgnoreCase("check")) {
            List<ItemStack> bountyItems = activeBounties.get(targetName.toLowerCase());
            if (bountyItems == null || bountyItems.isEmpty()) {
                player.sendMessage(Component.text("There is currently no active bounty on " + targetName + ".", NamedTextColor.YELLOW));
                return true;
            }

            Inventory checkGui = Bukkit.createInventory(null, 27, Component.text("Bounty Rewards: " + targetName));
            int slot = 0;
            for (ItemStack item : bountyItems) {
                if (slot >= 27) break;
                if (item != null && item.getType() != Material.AIR) {
                    checkGui.setItem(slot++, item.clone());
                }
            }

            player.openInventory(checkGui);
            player.sendMessage(Component.text("Viewing bounty items for " + targetName + ".", NamedTextColor.AQUA));
            return true;
        }

        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("--- Bounty Help ---", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text("/bounty <username> set - Open GUI to place a bounty", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/bounty <username> check - View current bounty", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/bounty claim - Claim your earned bounty rewards", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/bounty list - View active bounties", NamedTextColor.YELLOW));
    }

    public static void saveBountyData(DeceptionMissions plugin) {
        File file = new File(plugin.getDataFolder(), "bounties.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, List<ItemStack>> entry : activeBounties.entrySet()) {
            config.set("active." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<UUID, List<ItemStack>> entry : unclaimedBountyRewards.entrySet()) {
            config.set("unclaimed." + entry.getKey().toString(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save bounties.yml data!");
        }
    }

    public static void loadBountyData(DeceptionMissions plugin) {
        File file = new File(plugin.getDataFolder(), "bounties.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        activeBounties.clear();
        unclaimedBountyRewards.clear();

        if (config.isConfigurationSection("active")) {
            for (String key : config.getConfigurationSection("active").getKeys(false)) {
                List<?> rawList = config.getList("active." + key);
                if (rawList != null) {
                    List<ItemStack> items = new ArrayList<>();
                    for (Object obj : rawList) {
                        if (obj instanceof ItemStack is) items.add(is);
                    }
                    activeBounties.put(key, items);
                }
            }
        }

        if (config.isConfigurationSection("unclaimed")) {
            for (String key : config.getConfigurationSection("unclaimed").getKeys(false)) {
                List<?> rawList = config.getList("unclaimed." + key);
                if (rawList != null) {
                    List<ItemStack> items = new ArrayList<>();
                    for (Object obj : rawList) {
                        if (obj instanceof ItemStack is) items.add(is);
                    }
                    unclaimedBountyRewards.put(UUID.fromString(key), items);
                }
            }
        }
    }
}
