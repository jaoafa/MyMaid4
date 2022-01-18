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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

public class Event_LongTimeNoSee extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "久々に来た人に「お久しぶりです」と伝えます。";
    }

    @EventHandler
    public void OnJoin(PlayerJoinEvent event) {
        if (!MyMaidData.isMainDBActive()) {
            return;
        }
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT unix_timestamp(date) as ts FROM login WHERE uuid = ? ORDER BY id DESC");
            statement.setString(1, uuid);
            ResultSet res = statement.executeQuery();
            res.next();
            if (!res.next()) {
                return;
            }

            String last_str = res.getString("ts");
            long last = Long.parseLong(last_str);
            long now = System.currentTimeMillis() / 1000L;
            long sa = now - last;
            Main.getMyMaidLogger().info(MessageFormat.format("[LongTimeNoSee] {0}: {1}s (LAST: {2} / NOW: {3})",
                player.getName(),
                sa,
                last,
                now));
            Main.getMyMaidLogger().info("[LongTimeNoSee] " + player.getName() + ": last_str: " + last_str);
            if (sa < 2592000L) {
                return;
            }
            String formattedTime = formatTime(sa);
            chatFake(NamedTextColor.GOLD, "jaotan", MessageFormat.format("{0}さん、お久しぶりです！{1}ぶりですね！",
                player.getName(),
                formattedTime));
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    String formatTime(long sa) {
        StringBuilder builder = new StringBuilder();

        double year = Math.floor(sa / 31536000.0);
        int year_remain = (int) Math.floor(sa % 31536000L);
        if (year != 0) {
            builder.append(year).append("年");
        }
        int month = (int) Math.floor(year_remain / 2592000.0);
        int month_remain = (int) Math.floor(year_remain % 2592000L);
        if (month != 0) {
            builder.append(month).append("か月");
        }
        int day = (int) Math.floor(month_remain / 86400.0);
        int day_remain = (int) Math.floor(month_remain % 86400L);
        if (day != 0) {
            builder.append(day).append("日");
        }
        int hour = (int) Math.floor(day_remain / 3600.0);
        int hour_remain = (int) Math.floor(day_remain % 3600L);
        if (hour != 0) {
            builder.append(hour).append("時間");
        }
        int minute = (int) Math.floor(hour_remain / 60.0);
        if (minute != 0) {
            builder.append(minute).append("分");
        }
        int sec = (int) Math.floor(hour_remain % 60.0);
        if (sec != 0) {
            builder.append(sec).append("秒");
        }

        return builder.toString();
    }
}
