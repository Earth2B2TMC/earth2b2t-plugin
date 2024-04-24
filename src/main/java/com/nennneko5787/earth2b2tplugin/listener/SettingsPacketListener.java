package com.nennneko5787.earth2b2tplugin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsPacketListener extends PacketAdapter {
    private static final Map<UUID, String> playerLocales = new HashMap<>();

    public SettingsPacketListener(Plugin plugin) {
        super(plugin, PacketType.Play.Client.SETTINGS);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        // SETTINGS packet received
        Player player = event.getPlayer(); // Get the player associated with the event
        String locale = event.getPacket().getStrings().read(0); // Assuming locale is the first string in the packet
        playerLocales.put(player.getUniqueId(), locale); // Update player's locale
        System.out.println("Player " + player.getName() + " locale set to: " + locale);
    }

    public String getPlayerLocale(Player player) {
        return playerLocales.getOrDefault(player.getUniqueId(), "en_US");
    }

    public static Map<UUID, String> getPlayerLocales() {
        return playerLocales;
    }
}
