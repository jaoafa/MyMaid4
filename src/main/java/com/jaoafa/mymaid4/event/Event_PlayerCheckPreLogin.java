/*
 * jaoLicense
 *
 * Copyright (c) 2021 jao Minecraft Server
 *
 * The following license applies to this project: jaoLicense
 *
 * Japanese: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE.md
 * English: https://github.com/jaoafa/jao-Minecraft-Server/blob/master/jaoLICENSE-en.md
 */

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.MySQLDBManager;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class Event_PlayerCheckPreLogin extends MyMaidLibrary implements Listener {

    private static CityResponse getGeoIP(InetAddress ia) {
        JavaPlugin plugin = Main.getJavaPlugin();
        File file = new File(plugin.getDataFolder(), "GeoLite2-City.mmdb");
        if (!file.exists()) {
            plugin.getLogger().warning("GeoLite2-City.mmdb not found. Check Login failed.");
            return null;
        }

        try {
            DatabaseReader dr = new DatabaseReader.Builder(file).build();
            return dr.city(ia);
        } catch (IOException e) {
            plugin.getLogger().warning("IOException catched. getGeoIP failed.");
            e.printStackTrace();
            return null;
        } catch (GeoIp2Exception e) {
            plugin.getLogger().warning("GeoIp2Exception catched. getGeoIP failed.");
            e.printStackTrace();
            return null;
        }
    }

    private static void disallow(AsyncPlayerPreLoginEvent event, String message, String reason) {
        event.disallow(Result.KICK_FULL,
            ChatColor.RED + "[Login Denied! - Reason: " + reason + "]\n"
                + ChatColor.RESET + message
                + ChatColor.RESET + ChatColor.WHITE + "もしこの判定が誤判定と思われる場合は、公式Discordへお問い合わせください。");
        if (MyMaidData.getJaotanChannel() == null) return;
        MyMaidData.getJaotanChannel().sendMessage(
            "[MyMaid3-PreLoginCheck] " + event.getName() + " -> `" + reason + "`").queue();
    }

    @EventHandler(priority = EventPriority.LOW)
    public static void OnEvent_PlayerCheckPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        UUID uuid = event.getUniqueId();
        InetAddress ia = event.getAddress();
        String ip = ia.getHostAddress();
        String host = ia.getHostName();

        if (!MyMaidData.isMainDBActive()) return;
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            event.disallow(Result.KICK_OTHER, ChatColor.RED + "[DATABASE CONNECTION ERROR]\n" +
                "PLEASE CONTACT ADMINISTRATOR. (1)");
            return;
        }
        try {
            Connection conn = MySQLDBManager.getConnection();
            if (conn.isClosed() || !conn.isValid(3)) {
                event.disallow(Result.KICK_OTHER, ChatColor.RED + "[DATABASE CONNECTION ERROR]\n" +
                    "PLEASE CONTACT ADMINISTRATOR. (2)");
                return;
            }
        } catch (SQLException e) {
            event.disallow(Result.KICK_OTHER, ChatColor.RED + "[DATABASE CONNECTION ERROR]\n" +
                "PLEASE CONTACT ADMINISTRATOR. (3)");
            return;
        }

        Country country = null;
        String countryName = null;
        City city = null;
        String cityName = null;
        if (!(ia.isAnyLocalAddress() || ia.isLoopbackAddress()) && !ip.startsWith("192.168")) {
            CityResponse res = Event_PlayerCheckPreLogin.getGeoIP(ia);
            if (res != null) {
                country = res.getCountry();
                countryName = country.getName();
                city = res.getCity();
                cityName = city.getName();
            }
        }

        String permission = getPermissionMainGroup(Bukkit.getOfflinePlayer(uuid));
        if (country != null) {
            Main.getJavaPlugin().getLogger().info("Country: " + country.getName() + " (" + country.getIsoCode() + ")");
            Main.getJavaPlugin().getLogger().info("City: " + city.getName() + " (" + city.getGeoNameId() + ")");
        }
        Main.getJavaPlugin().getLogger().info("Permission: " + permission);

        // 「jaotan」というプレイヤー名は禁止
        if (name.equalsIgnoreCase("jaotan")) {
            Event_PlayerCheckPreLogin.disallow(event,
                ChatColor.WHITE + "あなたのMinecraftIDは、システムの運用上の問題によりログイン不可能と判断されました。\n"
                    + ChatColor.RESET + ChatColor.AQUA + "ログインするには、MinecraftIDを変更してください。",
                "UserName");
            return;
        }

        // 日本国外からのアクセスをすべて規制
        if (country != null && !countryName.equalsIgnoreCase("Japan")) {
            Event_PlayerCheckPreLogin.disallow(event,
                ChatColor.WHITE + "海外からのログインと判定されました。\n"
                    + ChatColor.RESET + ChatColor.AQUA + "当サーバでは、日本国外からのログインを禁止しています。",
                "Region restricted",
                countryName + " " + cityName);
            return;
        }

        String finalCountry = countryName;
        String finalCity = cityName;
        new BukkitRunnable() {
            @Override
            public void run() {
                MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
                if (MySQLDBManager == null) return;
                try {
                    Connection conn = MySQLDBManager.getConnection();
                    PreparedStatement statement = conn.prepareStatement(
                        "INSERT INTO login (player, uuid, ip, host, countryName, city, permission) VALUES (?, ?, ?, ?, ?, ?, ?);");
                    statement.setString(1, name); // player
                    statement.setString(2, uuid.toString()); // uuid
                    statement.setString(3, ip); // ip
                    statement.setString(4, host); // host
                    statement.setString(5, finalCountry); // countryName
                    statement.setString(6, finalCity); // city
                    statement.setString(7, permission); // permission
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    MyMaidLibrary.reportError(getClass(), e);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    private static void disallow(AsyncPlayerPreLoginEvent event, String message, String reason, String data) {

        event.disallow(Result.KICK_FULL,
            ChatColor.RED + "[Login Denied! - Reason: " + reason + "]\n"
                + ChatColor.RESET + message + "\n"
                + ChatColor.RESET + ChatColor.WHITE + "もしこの判定が誤判定と思われる場合は、公式Discordへお問い合わせください。");
        if (MyMaidData.getJaotanChannel() == null) return;
        MyMaidData.getJaotanChannel().sendMessage(
            "[MyMaid3-PreLoginCheck] " + event.getName() + " -> `" + reason + " (" + data + ")`").queue();
    }
}
