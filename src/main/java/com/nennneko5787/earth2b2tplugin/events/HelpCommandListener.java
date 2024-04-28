package com.nennneko5787.earth2b2tplugin.events;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class HelpCommandListener implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Earth2B2T Commands");
        sender.sendMessage(ChatColor.GOLD + "/help"+ ChatColor.RESET +" - This command / このコマンド。");
        sender.sendMessage(ChatColor.GOLD + "/wtp"+ ChatColor.RESET +" - Displays a GUI for switching worlds. / ワールドを切り替えるGUIを表示します。");
        return true;
    }
}
