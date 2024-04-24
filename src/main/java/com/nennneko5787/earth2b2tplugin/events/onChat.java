package com.nennneko5787.earth2b2tplugin.events;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.ChatColor;
import org.bukkit.event.EventPriority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geysermc.floodgate.api.FloodgateApi;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import java.nio.charset.StandardCharsets;

import com.nennneko5787.earth2b2tplugin.listener.SettingsPacketListener;
import java.util.UUID;
import java.net.URLEncoder;

public class onChat implements Listener {
	private static final Map<String, Map<String, String>> cache = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		FloodgateApi floodgateApi = FloodgateApi.getInstance();
		String hmessage = event.getMessage();
		String sourceLanguage = "";

		LunaChatAPI lunachatapi = ((LunaChatBukkit) Bukkit.getServer().getPluginManager().getPlugin("LunaChat")).getLunaChatAPI();

		if (floodgateApi.isFloodgatePlayer(event.getPlayer().getUniqueId())) {
			sourceLanguage = floodgateApi.getPlayer(event.getPlayer().getUniqueId()).getLanguageCode().toLowerCase();
		} else {
			Map<UUID, String> playerLocales = SettingsPacketListener.getPlayerLocales();
			sourceLanguage = playerLocales.get(event.getPlayer().getUniqueId());
		}
		final String finalSourceLanguage = sourceLanguage;

		Boolean skipJapanize = false;
		if ( !skipJapanize &&
				( event.getMessage().getBytes(StandardCharsets.UTF_8).length > event.getMessage().length() ||
					event.getMessage().matches("[ \\uFF61-\\uFF9F]+") ) ) {
			skipJapanize = true;
		}
		Bukkit.getServer().getLogger().info(sourceLanguage);

		if (sourceLanguage == "ja_jp" && !event.getMessage().startsWith("$") && lunachatapi.isPlayerJapanize(event.getPlayer().getName()) && !skipJapanize) {
			String japanizedMessage = hmessage; // メッセージを取得
			hmessage = lunachatapi.japanize(japanizedMessage, JapanizeType.GOOGLE_IME); // 日本語化したメッセージをセット
		}

		if (hmessage.startsWith("$")) { // メッセージが$で始まる場合
			hmessage = hmessage.substring(1); // 先頭の$を削除
		}

		final String message = hmessage;

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Earth2B2T");

		for (Player recipient : event.getRecipients()) {
			final String targetLanguage;
			if (floodgateApi.isFloodgatePlayer(recipient.getUniqueId())) {
				targetLanguage = floodgateApi.getPlayer(recipient.getUniqueId()).getLanguageCode();
			} else {
				targetLanguage = recipient.getLocale();
			}

			if (!sourceLanguage.equals(targetLanguage)) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					String translatedMessage = sendHttpRequest(message, finalSourceLanguage, targetLanguage);
					
					if (translatedMessage == null) {
						translatedMessage = "§cFailed to translate / 翻訳に失敗しました";
					}
					
					String formattedMessage = "[" + ChatColor.LIGHT_PURPLE + "Translated" + ChatColor.RESET + "]<" + event.getPlayer().getDisplayName() + "> " + translatedMessage + " " + ChatColor.GRAY + event.getMessage();
					
					recipient.sendMessage(formattedMessage);
				});
			}
		}
	}

	private String sendHttpRequest(String originalMessage, String sourceLanguage, String targetLanguage) {
		String cachedTranslation = getTranslationFromCache(originalMessage, sourceLanguage, targetLanguage);
		if (cachedTranslation != null) {
			return cachedTranslation;
		}

		String encodedMessage = URLEncoder.encode(originalMessage, StandardCharsets.UTF_8);

		try {
			String urlString = "https://script.google.com/macros/s/AKfycbzzGZ0cx9PlLvY7vZDzYOrscdoaJ_uX-aXgbisFK1yFA0PR2vZvSZ6uqAACcbq5NgTCHA/exec?text="
					+ encodedMessage + "&source=" + sourceLanguage + "&target=" + targetLanguage;
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine;
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				Bukkit.getServer().getLogger().info(response.toString());
				String[] parts = response.toString().split("\n");
				if (parts.length > 0 && parts[0].equals("200")) {
					if (parts.length > 1) {
						cacheTranslation(originalMessage, sourceLanguage, targetLanguage, parts[1]);
						return parts[1];
					}else{
						return null;
					}
				}else{
					return null;
				}
			} else {
				System.out.println("HTTP request failed with status code: " + responseCode);
			}
		} catch (IOException e) {
			System.out.println("Failed to send HTTP request: " + e.getMessage());
		}

		return null;
	}

	private String getTranslationFromCache(String originalMessage, String sourceLanguage, String targetLanguage) {
		Map<String, String> sourceCache = cache.getOrDefault(sourceLanguage, new HashMap<>());
		return sourceCache.getOrDefault(originalMessage + targetLanguage, null);
	}

	private void cacheTranslation(String originalMessage, String sourceLanguage, String targetLanguage, String translatedMessage) {
		Map<String, String> sourceCache = cache.getOrDefault(sourceLanguage, new HashMap<>());
		sourceCache.put(originalMessage + targetLanguage, translatedMessage);
		cache.put(sourceLanguage, sourceCache);
	}
}
