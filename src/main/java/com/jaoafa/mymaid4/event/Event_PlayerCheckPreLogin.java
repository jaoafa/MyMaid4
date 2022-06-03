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
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class Event_PlayerCheckPreLogin extends MyMaidLibrary implements Listener, EventPremise {
    @EventHandler(priority = EventPriority.LOW)
    public static void OnEvent_PlayerCheckPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        UUID uuid = event.getUniqueId();
        InetAddress ia = event.getAddress();
        String ip = ia.getHostAddress();
        String host = ia.getHostName();

        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getMyMaidLogger().warning("開発サーバのため、ログインチェックは動作しません。");
            return;
        }

        if (!MyMaidData.isMainDBActive()) return;
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            disallow(event, Component.text("サーバ側のシステムエラーによりログインできませんでした。"), "DB ERROR (1)");
            return;
        }
        try {
            Connection conn = MySQLDBManager.getConnection();
            if (conn.isClosed() || !conn.isValid(3)) {
                disallow(event, Component.text("サーバ側のシステムエラーによりログインできませんでした。"), "DB ERROR (2)");
                return;
            }
        } catch (SQLException e) {
            disallow(event, Component.text("サーバ側のシステムエラーによりログインできませんでした。"), "DB ERROR (3)");
            MyMaidLibrary.reportError(Event_PlayerCheckPreLogin.class, e);
            return;
        }

        GeoIP geoip = !(ia.isAnyLocalAddress() || ia.isLoopbackAddress()) && !ip.startsWith("192.168") ?
            getGeoIP(ia) : null;
        if (geoip != null && geoip.country() != null && geoip.city() != null) {
            Main.getJavaPlugin().getLogger().info("Country: " + geoip.country().getName() + " (" + geoip.country().getIsoCode() + ")");
            Main.getJavaPlugin().getLogger().info("City: " + geoip.city().getName() + " (" + geoip.city().getGeoNameId() + ")");
        }

        String permission = getPermissionMainGroup(Bukkit.getOfflinePlayer(uuid));
        Main.getJavaPlugin().getLogger().info("Permission: " + permission);

        // 「jaotan」というプレイヤー名は禁止
        if (name.equalsIgnoreCase("jaotan")) {
            disallow(event, Component.text().append(
                Component.text("あなたのMinecraftIDは、システムの運用上の問題によりログイン不可能と判断されました。", NamedTextColor.WHITE),
                Component.text("ログインするには、MinecraftIDを変更してください。", NamedTextColor.AQUA)
            ).build(), "UserName");
            return;
        }

        if (geoip != null && (geoip.country() == null || geoip.country().getIsoCode() == null)) {
            // Unknown
            disallow(event, Component.text().append(
                Component.text("接続元国情報の取得に失敗しました。", NamedTextColor.WHITE),
                Component.text("当サーバでは、日本国外からのログインを禁止しています。", NamedTextColor.AQUA),
                Component.text("あなたの接続元国情報を取得できなかったため、ログインを制限しました。", NamedTextColor.AQUA)
            ).build(), "Region unknown");
        } else if (geoip != null && !geoip.country().getIsoCode().equals("JP")) {
            // 日本国外からのアクセスをすべて規制

            // Japan
            // Japan Tokyo
            String location = geoip.city() != null ?
                geoip.country().getName() + " " + geoip.city().getName() :
                geoip.country().getName();

            disallow(event, Component.text().append(
                Component.text("海外からのログインと判定されました。", NamedTextColor.WHITE),
                Component.text("当サーバでは、日本国外からのログインを禁止しています。", NamedTextColor.AQUA)
            ).build(), "Region restricted", location);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
                if (MySQLDBManager == null) return;
                try {
                    Connection conn = MySQLDBManager.getConnection();
                    try (PreparedStatement statement = conn.prepareStatement(
                        "INSERT INTO login (player, uuid, ip, host, countryName, city, permission) VALUES (?, ?, ?, ?, ?, ?, ?);")) {
                        statement.setString(1, name); // player
                        statement.setString(2, uuid.toString()); // uuid
                        statement.setString(3, ip); // ip
                        statement.setString(4, host); // host
                        statement.setString(5, geoip != null && geoip.country() != null ? geoip.country().getName() : null); // countryName
                        statement.setString(6, geoip != null && geoip.city() != null ? geoip.city().getName() : null); // city
                        statement.setString(7, permission); // permission
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    MyMaidLibrary.reportError(getClass(), e);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    private static GeoIP getGeoIP(InetAddress ia) {
        JavaPlugin plugin = Main.getJavaPlugin();
        File file = new File(plugin.getDataFolder(), "GeoLite2-City.mmdb");
        if (!file.exists()) {
            plugin.getLogger().warning("GeoLite2-City.mmdb not found. Check Login failed.");
            return null;
        }

        try (DatabaseReader dr = new DatabaseReader.Builder(file).build()) {
            CityResponse response = dr.city(ia);
            return new GeoIP(response.getCountry(), response.getCity());
        } catch (IOException e) {
            plugin.getLogger().warning("IOException caught. getGeoIP failed.");
            MyMaidLibrary.reportError(Event_PlayerCheckPreLogin.class, e);
            return null;
        } catch (GeoIp2Exception e) {
            plugin.getLogger().warning("GeoIp2Exception caught. getGeoIP failed.");
            MyMaidLibrary.reportError(Event_PlayerCheckPreLogin.class, e);
            return null;
        }
    }

    record GeoIP(Country country, City city) {
    }

    private static void disallow(AsyncPlayerPreLoginEvent event, Component message, String reason) {
        disallow(event, message, reason, null);
    }

    private static void disallow(AsyncPlayerPreLoginEvent event, Component message, String reason, String data) {
        Component component = Component.text().append(
            Component.text("[Login Denied! - Reason: " + reason + "]", NamedTextColor.RED),
            Component.newline(),
            message,
            Component.newline(),
            Component.text("もしこの判定が誤判定と思われる場合は、公式Discordへお問い合わせください。", NamedTextColor.WHITE)
        ).build();
        event.disallow(Result.KICK_FULL, component);
        if (MyMaidData.getJaotanChannel() == null) return;
        String content = data != null ?
            "[MyMaid4-PreLoginCheck] " + event.getName() + " -> `" + reason + " (" + data + ")`" :
            "[MyMaid4-PreLoginCheck] " + event.getName() + " -> `" + reason + "`";
        MyMaidData.getJaotanChannel().sendMessage(content).queue();
    }

    @Override
    public String description() {
        return "プレイヤーログイン前に各種チェック等を行います。";
    }
}
