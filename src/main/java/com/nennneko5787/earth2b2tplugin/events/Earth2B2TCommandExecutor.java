package com.nennneko5787.earth2b2tplugin.events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;

public class Earth2B2TCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§bEa§2rth§b2B2§2T§r Version: "+Bukkit.getServer().getPluginManager().getPlugin("Earth2B2T").getDescription().getVersion());
        return true;
    }
}
