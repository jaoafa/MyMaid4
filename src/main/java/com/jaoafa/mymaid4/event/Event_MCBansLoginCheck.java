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

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.MySQLDBManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.UUID;

public class Event_MCBansLoginCheck extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時、MCBansのキャッシュデータベースを確認しGBanされていたりReputationが足りなくないかを確認します。";
    }

    @EventHandler
    public void OnLoginCheck(AsyncPlayerPreLoginEvent event) {
        // MCBansが落ちている場合を考慮してjaoafaデータベースからチェック

        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getMyMaidLogger().warning("開発サーバのため、MCBansチェックは動作しません。");
            return;
        }

        // Reputationチェック
        String name = event.getName();
        UUID uuid = event.getUniqueId();

        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            return;
        }

        try {
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM mcbans WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet res = statement.executeQuery()) {
                    if (res.next()) {
                        float reputation = res.getFloat("reputation");
                        if (reputation < 3) {
                            // 3未満は規制
                            Component component = Component.text().append(
                                Component.text("----- MCBans Checker -----", NamedTextColor.RED),
                                Component.newline(),
                                Component.text("Access denied.", NamedTextColor.WHITE),
                                Component.newline(),
                                Component.text("Your reputation is below this server's threshold.", NamedTextColor.WHITE)
                            ).build();
                            event.disallow(Result.KICK_BANNED, component);
                            return;
                        }
                        if (reputation != 10) {
                            sendAMR(Component.text().append(
                                Component.text("[MCBansChecker]", NamedTextColor.RED),
                                Component.space(),
                                Component.text(name, NamedTextColor.GREEN),
                                Component.space(),
                                Component.text("reputation: ", NamedTextColor.GREEN),
                                Component.space(),
                                Component.text(reputation, NamedTextColor.GREEN)
                            ).build());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return;
        }

        // jaoでBan済みかどうか
        try {
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM mcbans_jaoafa WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet res = statement.executeQuery()) {
                    if (res.next()) {
                        int banid = res.getInt("banid");
                        String type = res.getString("type");
                        String reason = res.getString("reason");
                        Component component = Component.text().append(
                            Component.text("----- MCBans Checker -----", NamedTextColor.RED),
                            Component.newline(),
                            Component.text("Access denied.", NamedTextColor.WHITE),
                            Component.newline(),
                            Component.text("Reason: " + reason, NamedTextColor.WHITE),
                            Component.newline(),
                            Component.text("Ban type: " + type, NamedTextColor.WHITE),
                            Component.newline(),
                            Component.text("https://mcbans.com/ban/" + banid, NamedTextColor.WHITE)
                                .clickEvent(ClickEvent.openUrl("https://mcbans.com/ban/" + banid))
                        ).build();
                        event.disallow(Result.KICK_BANNED, component);
                    }
                }
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    @EventHandler
    public void OnLoginAfterCheck(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getMyMaidLogger().warning("開発サーバのため、MCBansチェックは動作しません。");
            return;
        }

        new BukkitRunnable() {
            public void run() {
                try {
                    String url = String.format("https://api.jaoafa.com/users/mcbans/%s", uuid);

                    Main.getMyMaidLogger().info(MessageFormat.format("OnLoginAfterCheck: APIサーバへの接続を開始: {0} -> {1}", player.getName(), url));
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).get().build();
                    JSONObject json;
                    try (Response response = client.newCall(request).execute()) {
                        if (response.code() != 200) {
                            Main.getMyMaidLogger().info("OnLoginAfterCheck: APIサーバへの接続に失敗: %s -> %d%n".formatted(url, response.code()));
                            response.close();
                            return;
                        }
                        try (ResponseBody body = response.body()) {
                            if (body == null) {
                                Main.getMyMaidLogger().info(MessageFormat.format("OnLoginAfterCheck: APIサーバへの接続に失敗: {0} -> response.body() is null.", url));
                                response.close();
                                return;
                            }

                            json = new JSONObject(body.string());
                        }
                    }

                    if (!json.has("status")) {
                        Main.getMyMaidLogger().info("OnLoginAfterCheck: レスポンスの解析に失敗: status not found.");
                        return;
                    }
                    if (!json.getBoolean("status")) {
                        Main.getMyMaidLogger().info("OnLoginAfterCheck: レスポンスの解析に失敗: status not boolean.");
                        return;
                    }
                    if (!json.has("data")) {
                        Main.getMyMaidLogger().info("OnLoginAfterCheck: レスポンスの解析に失敗: data not found.");
                        return;
                    }

                    JSONObject data = json.getJSONObject("data");
                    double reputation = data.getDouble("reputation");
                    int globalCount = data.getInt("global");
                    int localCount = data.getInt("local");

                    sendAMR(Component.text().append(
                        Component.text("[MCBans]"),
                        Component.space(),
                        Component.text(String.format("%s -> %s (G:%d | L:%d)", player.getName(), reputation, globalCount, localCount), NamedTextColor.GRAY)
                    ).build());
                    if (reputation < 3) {
                        // 3未満はキック
                        Component component = Component.text().append(
                            Component.text("----- MCBans Checker -----", NamedTextColor.RED),
                            Component.newline(),
                            Component.text("Access denied.", NamedTextColor.WHITE),
                            Component.newline(),
                            Component.text("Your reputation is below this server's threshold.", NamedTextColor.WHITE)
                        ).build();
                        new BukkitRunnable() {
                            public void run() {
                                player.kick(component);
                            }
                        }.runTask(Main.getJavaPlugin());
                    }
                } catch (SocketTimeoutException e) {
                    Main.getMyMaidLogger().info("OnLoginAfterCheck: Timeout");
                } catch (IOException e) {
                    Main.getMyMaidLogger().info("OnLoginAfterCheck: IOException Error...");
                    MyMaidLibrary.reportError(getClass(), e);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}
