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

public class MCBans {
    final OfflinePlayer offplayer;
    boolean isFound = false;

    int globalCount = -1;
    int[] global_ids = new int[]{};
    double reputation = -1;
    int localCount = -1;
    int[] local_ids = new int[]{};
    String updated_at = null;

    public MCBans(OfflinePlayer offplayer) throws IOException {
        this.offplayer = offplayer;

        String url = "https://api.jaoafa.com/users/mcbans/" + offplayer.getUniqueId();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            response.close();
            return;
        }

        ResponseBody body = response.body();
        if (body == null) throw new RuntimeException();

        JSONObject json = new JSONObject(body.string());
        response.close();

        if (!json.has("status")) {
            return;
        }
        if (!json.getBoolean("status")) {
            return;
        }

        if (!json.has("data")) {
            return;
        }

        JSONObject data = json.getJSONObject("data");

        reputation = data.getDouble("reputation");
        globalCount = data.getInt("global");
        localCount = data.getInt("local");

        JSONArray global_ids_array = data.getJSONArray("global_ids");
        global_ids = new int[global_ids_array.length()];
        for (int i = 0; i < global_ids_array.length(); i++) {
            global_ids[i] = global_ids_array.getInt(i);
        }

        JSONArray local_ids_array = data.getJSONArray("local_ids");
        local_ids = new int[local_ids_array.length()];
        for (int i = 0; i < local_ids_array.length(); i++) {
            local_ids[i] = local_ids_array.getInt(i);
        }

        updated_at = data.getString("updated_at");

        isFound = true;
    }

    public boolean isFound() {
        return isFound;
    }

    public double getReputation() {
        return reputation;
    }

    public int getGlobalCount() {
        return globalCount;
    }

    public int[] getGlobalBanIds() {
        return global_ids;
    }

    public int getLocalCount() {
        return localCount;
    }

    public int[] getLocalBanIds() {
        return local_ids;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public static class Ban {
        final int banid;

        String mcid;
        String banned_by;
        String reason;
        String server;
        String type;
        double lostrep;
        String date;
        String ban_updated_at;

        public Ban(int banid) throws IOException {
            this.banid = banid;

            String url = "https://api.jaoafa.com/users/mcbans/ban/" + banid;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).get().build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                response.close();
                return;
            }

            ResponseBody body = response.body();
            if (body == null) throw new RuntimeException();

            JSONObject json = new JSONObject(body.string());
            response.close();

            if (!json.has("status")) {
                return;
            }
            if (!json.getBoolean("status")) {
                return;
            }

            if (!json.has("data")) {
                return;
            }
            JSONObject data = json.getJSONObject("data");

            mcid = data.getString("mcid");
            banned_by = data.getString("banned_by");
            reason = data.getString("reason");
            server = data.getString("server");
            type = data.getString("type");
            lostrep = data.getDouble("lostrep");
            date = data.getString("date");
            ban_updated_at = data.getString("updated_at");
        }

        public int getBanID() {
            return banid;
        }

        public String getMinecraftID() {
            return mcid;
        }

        public String getBannedBy() {
            return banned_by;
        }

        public String getReason() {
            return reason;
        }

        public String getServer() {
            return server;
        }

        public String getType() {
            return type;
        }

        public double getLostReputation() {
            return lostrep;
        }

        public String getDate() {
            return date;
        }

        public String getUpdatedAt() {
            return ban_updated_at;
        }
    }
}