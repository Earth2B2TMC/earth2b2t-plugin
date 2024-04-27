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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		FloodgateApi floodgateApi = FloodgateApi.getInstance();
		String hmessage = event.getMessage();
		String sourceLanguage = "";

		LunaChatAPI lunachatapi = ((LunaChatBukkit) Bukkit.getServer().getPluginManager().getPlugin("LunaChat")).getLunaChatAPI();

		if (floodgateApi.isFloodgatePlayer(event.getPlayer().getUniqueId())) {
			sourceLanguage = floodgateApi.getPlayer(event.getPlayer().getUniqueId()).getLanguageCode().toLowerCase();
		} else {
			sourceLanguage = event.getPlayer().getLocale().toLowerCase();
    }

		Boolean skipJapanize = false;
		if ( ( event.getMessage().getBytes(StandardCharsets.UTF_8).length > event.getMessage().length() ||
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

      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        Translation translation  = null;
        String translatedMessage = null;
        try {
          translation = translate(message, targetLanguage);
          translatedMessage = translation.text;
        } catch (Exception e) {
          System.out.println("§cFailed to translate / 翻訳に失敗しました");
          return;
        }

        if( targetLanguage.startsWith(translation.lang ) )
          return;

        String formattedMessage = 
          ChatColor.LIGHT_PURPLE + "[" + 
          ChatColor.RESET        + "TL: " + 
          ChatColor.BLUE         + translation.lang + 
          ChatColor.RESET        + " -> " +
          ChatColor.GREEN        + targetLanguage + 
          ChatColor.LIGHT_PURPLE + "] " +
          ChatColor.RESET        + "<" + event.getPlayer().getDisplayName() + "> " + translatedMessage + " " + 
          ChatColor.GRAY         + event.getMessage();
        
        recipient.sendMessage(formattedMessage);
      });
		}
	}

  public static class Translation {
    public Translation() {
      lang = "";
      text = "";
    }
    
    public String lang;
    public String text;
  };

  private static Translation extractTranslatedText(String jsonResponse) {
    String[] parts = jsonResponse.split("\"");
    System.out.println( jsonResponse );

    Translation translation = new Translation();

    if (parts.length >= 2) {
      translation.text = parts[1];
      translation.lang = parts[parts.length - 2];
    }

    return translation;
  }

  public static Translation translate(String text, String targetLanguage) throws IOException {
      String encodedText = URLEncoder.encode(text, "UTF-8");
      String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLanguage + "&dt=t&q=" + encodedText;

      URL url = new URL(urlStr);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");

      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      StringBuilder response = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
          response.append(line);
      }
      reader.close();

      // JSON 応答から翻訳されたテキストを抽出します
      Translation translatedText = extractTranslatedText(response.toString());
      return translatedText;
  }
}
