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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Event_OnlineTimeDB extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時間を計測し、データベースに記録します。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnEvent_LoginDBInsert(PlayerJoinEvent event) {
        if (!MyMaidData.isMainDBActive()) {
            return;
        }
        Player player = event.getPlayer();
        int onlineTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        if (!exists(player)) {
            create(player);
        }
        change_onlineTime(player, onlineTime);
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnEvent_QuitDBInsert(PlayerQuitEvent event) {
        if (!MyMaidData.isMainDBActive()) {
            return;
        }
        Player player = event.getPlayer();
        int onlineTime = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
        if (!exists(player)) {
            create(player);
        }
        change_onlineTime(player, onlineTime);
    }

    private void create(Player player) {
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement("INSERT INTO onlinetime (player, uuid) VALUES (?, ?);")) {
                statement.setString(1, player.getName());
                statement.setString(2, player.getUniqueId().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
        }
    }

    private boolean exists(Player player) {
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM onlinetime WHERE uuid = ? ORDER BY id DESC")) {
                statement.setString(1, player.getUniqueId().toString()); // uuid
                try (ResultSet res = statement.executeQuery()) {
                    return res.next();
                }
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
        }
        return false;
    }

    private void change_onlineTime(Player player, int value) {
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            PreparedStatement statement = conn.prepareStatement("UPDATE onlinetime SET onlinetime = ? WHERE uuid = ?;");
            statement.setInt(1, value);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            reportError(getClass(), e);
        }
    }
}
