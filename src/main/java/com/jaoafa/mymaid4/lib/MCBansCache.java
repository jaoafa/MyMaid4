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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.OfflinePlayer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCBansCache {
    private final OfflinePlayer player;
    private final int playerId;
    private final double reputation;
    private final List<Ban> bans;
    private final List<Ban> issuedBans;
    private final List<Server> ownedServers;
    private final Date lastCheckedAt;

    private MCBansCache(OfflinePlayer player, int playerId, double reputation, List<Ban> bans, List<Ban> issuedBans, List<Server> ownedServers, Date lastCheckedAt) {
        this.player = player;
        this.playerId = playerId;
        this.reputation = reputation;
        this.bans = bans;
        this.issuedBans = issuedBans;
        this.ownedServers = ownedServers;
        this.lastCheckedAt = lastCheckedAt;
    }

    public static MCBansCache get(OfflinePlayer player) throws IOException {
        String url = "https://cache-mcbans.amatama.net/player/%s".formatted(player.getUniqueId());
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        JSONObject json;
        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                response.close();
                return null;
            }
            try (ResponseBody body = response.body()) {
                if (body == null) throw new RuntimeException();

                json = new JSONObject(body.string());
            }
        }
        return new MCBansCache(player, json.getInt("playerId"), json.getDouble("reputation"), parseBans(json.getJSONArray("bans")), parseBans(json.getJSONArray("issuedBans")), parseServer(json.getJSONArray("ownedServers")), new Date(json.getLong("lastCheckedAt")));
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getReputation() {
        return reputation;
    }

    public List<Ban> getBans() {
        return bans;
    }

    public int getGlobalCount() {
        return Math.toIntExact(bans.stream().filter(ban -> ban.type().equals("global")).count());
    }

    public List<Ban> getGlobalBans() {
        return bans.stream().filter(ban -> ban.type().equals("global")).toList();
    }

    public int getLocalCount() {
        return Math.toIntExact(bans.stream().filter(ban -> ban.type().equals("local")).count());
    }

    public List<Ban> getLocalBans() {
        return bans.stream().filter(ban -> ban.type().equals("local")).toList();
    }

    public List<Ban> getIssuedBans() {
        return issuedBans;
    }

    public List<Server> getOwnedServers() {
        return ownedServers;
    }

    public Date getLastCheckedAt() {
        return lastCheckedAt;
    }

    private static List<Ban> parseBans(JSONArray array) {
        List<Ban> bans = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            bans.add(i, new Ban(json.getInt("banId"), json.getString("type"), json.getString("reason"), parseDate(json.getString("bannedAt")), parseDate(json.getString("lastCheckedAt")), parseDate(json.getString("createdAt")), parseDate(json.getString("updatedAt"))));
        }
        return bans;
    }

    private static List<Server> parseServer(JSONArray array) {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            servers.add(i, new Server(json.getInt("serverId"), json.getString("address"), json.getDouble("reputation"), parseDate(json.getString("registeredAt")), parseDate(json.getString("lastCheckedAt")), parseDate(json.getString("createdAt")), parseDate(json.getString("updatedAt"))));
        }
        return servers;
    }

    private static Date parseDate(String str) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return df.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    public record Ban(int banId, String type, String reason, Date bannedAt, Date lastCheckedAt, Date createdAt,
                      Date updatedAt) {
        public BanDetails retrieveDetails() throws IOException {
            String url = "https://cache-mcbans.amatama.net/ban/%s".formatted(banId);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            JSONObject json;
            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 200) {
                    response.close();
                    return null;
                }
                try (ResponseBody body = response.body()) {
                    if (body == null) throw new RuntimeException();

                    json = new JSONObject(body.string());
                }
            }
            return new BanDetails(json.getJSONObject("server").getInt("serverId"), json.getJSONObject("server").getString("address"), json.getJSONObject("bannedBy").getInt("playerId"), json.getJSONObject("bannedBy").getString("name"), json.getJSONObject("bannedBy").getString("uuid"));
        }
    }

    public record BanDetails(int serverId, String serverAddress, int bannedById, String bannedByName,
                             String bannedByUUID) {
    }

    public record Server(int serverId, String address, double reputation, Date registeredAt, Date lastCheckedAt,
                         Date createdAt, Date updatedAt) {
    }
}
