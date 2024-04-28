package com.nennneko5787.earth2b2tplugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.nennneko5787.earth2b2tplugin.events.onJoinPlayer;
import com.nennneko5787.earth2b2tplugin.events.onQuitPlayer;
import com.nennneko5787.earth2b2tplugin.events.onChat;
import com.nennneko5787.earth2b2tplugin.events.Earth2B2TCommandExecutor;
import com.nennneko5787.earth2b2tplugin.events.HelpCommandListener;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.nennneko5787.earth2b2tplugin.listener.SettingsPacketListener;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Earth2B2T Plugin is enabling now...");

        getLogger().info("registering events...");

        getServer().getPluginManager().registerEvents(new onJoinPlayer(), this);
        getServer().getPluginManager().registerEvents(new onQuitPlayer(), this);
        getServer().getPluginManager().registerEvents(new onChat(), this);

        new updatePlayerTabList().runTaskTimerAsynchronously(this, 0, 10);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        Plugin plugin = getServer().getPluginManager().getPlugin("Earth2B2T");
        protocolManager.addPacketListener(new SettingsPacketListener(plugin));

        getCommand("earth2b2t").setExecutor(new Earth2B2TCommandExecutor());
        getCommand("help").setExecutor(new HelpCommandListener());

        getLogger().info("Events registered");

        getLogger().info("Earth2B2T Plugin enabled now!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Earth2B2T is Disabled!");
    }

    private static class updatePlayerTabList extends BukkitRunnable {
        @Override
        public void run() {
            double[] ticksperseconds = Bukkit.getTPS();
            double tps = Math.floor(ticksperseconds[2]);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListHeaderFooter("\n    §bEa§2rth§b2B2§2T§r    \n    Players online / Max: "+ Bukkit.getOnlinePlayers().size() +" / "+ Bukkit.getMaxPlayers() +"    \n", "    Ping: "+ player.getPing() +"ms TPS: "+ tps +"     ");
            }
        }
    }
}
