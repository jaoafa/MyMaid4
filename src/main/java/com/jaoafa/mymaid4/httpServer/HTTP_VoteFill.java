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

package com.jaoafa.mymaid4.httpServer;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.event.Event_Vote;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMCJP;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMono;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class HTTP_VoteFill implements HttpHandler {
    int statusCode = 200;
    String responseBody = "{}";

    public void handle(HttpExchange t) throws IOException {
        Main.getJavaPlugin().getLogger().info(String.format("%s %s %s", t.getRequestMethod(), t.getRequestURI().toString(), t.getProtocol()));

        InputStream is = t.getRequestBody();
        String requestBody = InputStreamToString(is);
        is.close();

        Headers resHeaders = t.getResponseHeaders();
        resHeaders.set("Content-Type", "application/json");
        resHeaders.set("Last-Modified",
            ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME));

        PluginDescriptionFile desc = Main.getJavaPlugin().getDescription();
        String nowVer = desc.getVersion();
        resHeaders.set("Server", "MyMaid Server (" + nowVer + ")");

        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            autoFillProcess(requestBody);
        } else {
            createResponse(400, false, "Invalid method.");
        }

        long contentLength = responseBody.getBytes(StandardCharsets.UTF_8).length;
        t.sendResponseHeaders(statusCode, contentLength);

        OutputStream os = t.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }

    void autoFillProcess(String requestBody) {
        JSONObject request;
        try {
            request = new JSONObject(requestBody);
        } catch (JSONException e) {
            createResponse(400, false, String.format("Invalid JSON: %s", e.getMessage()));
            return;
        }
        if (!request.has("name") || !request.has("uuid") || !request.has("created_at") || !request.has("service")) {
            createResponse(400, false, "Missing uuid, created_at, service");
            return;
        }

        String name = request.getString("name");
        UUID uuid;
        try {
            uuid = UUID.fromString(request.getString("uuid"));
        } catch (IllegalArgumentException e) {
            createResponse(400, false, String.format("Missing uuid: %s", e.getMessage()));
            return;
        }

        long created_at = request.getLong("created_at"); // unixtime
        String service = request.getString("service");

        Main.getJavaPlugin().getLogger().info(String.format("[AutoFill-%s] %s (%s) %d", service, name, uuid, created_at));

        OfflinePlayer offplayer = Bukkit.getOfflinePlayer(uuid);
        try {
            if (service.equalsIgnoreCase("mcjp")) {
                PlayerVoteDataMCJP pvd = new PlayerVoteDataMCJP(offplayer);
                long last = pvd.getLastVoteUnixTime();
                long insert_time = Math.max(created_at, last);

                int oldVote = pvd.getVoteCount();
                boolean isTodayFirst = PlayerVoteDataMCJP.isTodayFirstVote();
                pvd.add(insert_time);
                int newVote = pvd.getVoteCount();

                Event_Vote.successNotifyMinecraftJP(name, oldVote, newVote, true);
                Event_Vote.checkjSA(offplayer, isTodayFirst, newVote);

                createResponse(200, true, String.format("Successful. insert_time = %d", insert_time));
            } else if (service.equalsIgnoreCase("mono")) {
                PlayerVoteDataMono pvd = new PlayerVoteDataMono(offplayer);
                long last = pvd.getLastVoteUnixTime();
                long insert_time = Math.max(created_at, last);

                int oldVote = pvd.getVoteCount();
                boolean isTodayFirst = PlayerVoteDataMono.isTodayFirstVote();
                pvd.add(insert_time);
                int newVote = pvd.getVoteCount();

                Event_Vote.successNotifyMonocraftNet(name, oldVote, newVote, true);
                Event_Vote.checkjSA(offplayer, isTodayFirst, newVote);

                createResponse(200, true, String.format("Successful. insert_time = %d", insert_time));
            } else {
                createResponse(400, false, "Missing service");
            }
        } catch (SQLException e) {
            createResponse(500, false, "Caught SQLException: " + e.getMessage());
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    void createResponse(int statusCode, boolean status, String message) {
        this.statusCode = statusCode;
        JSONObject obj = new JSONObject();
        obj.put("status", status);
        obj.put("message", message);
        this.responseBody = obj.toString();
    }

    String InputStreamToString(InputStream is) throws IOException {
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1024];
        int numRead;
        while (0 <= (numRead = reader.read(buf))) {
            builder.append(buf, 0, numRead);
        }
        reader.close();
        return builder.toString();
    }
}