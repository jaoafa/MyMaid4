/*
 * jaoLicense
 *
 * Copyright (c) 2022 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import okhttp3.*;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.IOException;

public class MeboChatBot {
    private final String apiKey;
    private final String agentId;

    public MeboChatBot(String apiKey, String agentId) {
        this.apiKey = apiKey;
        this.agentId = agentId;
    }

    public MeboResponse chat(Player player, String text) {
        try {
            String url = "https://api-mebo.dev/api";
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            json.put("api_key", apiKey);
            json.put("agent_id", agentId);
            json.put("utterance", text);
            json.put("uid", player.getUniqueId().toString());

            RequestBody requestBody = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=UTF-8"));
            Request request = new Request.Builder().url(url).post(requestBody).build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                response.close();
                return new MeboResponse(false, 0, "HTTP Error: " + response.code(), null);
            }

            ResponseBody body = response.body();
            if (body == null) throw new RuntimeException();

            JSONObject jsonObject = new JSONObject(body.string());
            response.close();

            if (!jsonObject.has("bestResponse")) {
                return new MeboResponse(false, 0, "No bestResponse", null);
            }
            // Main.getMyMaidLogger().info("MeboChatBot: " + jsonObject.getJSONObject("bestResponse").toString());

            return new MeboResponse(
                true,
                jsonObject.getJSONObject("bestResponse").getDouble("score"),
                jsonObject.getJSONObject("bestResponse").getString("utterance"),
                jsonObject.getJSONObject("bestResponse").getString("url")
            );
        } catch (IOException e) {
            e.printStackTrace();
            return new MeboResponse(false, 0, "IOException", null);
        }
    }

    public record MeboResponse(boolean status, double score, String rawMessage, String url) {
        public Component message() {
            if (url != null) {
                return Component
                    .text(rawMessage)
                    .append(
                        Component.text(url, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("クリックすると「" + url + "」をブラウザで開きます。")))
                            .clickEvent(ClickEvent.openUrl(url))
                    );
            }
            return Component.text(rawMessage);
        }
    }
}
