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

package com.jaoafa.mymaid4.lib;

import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Historyjao {
    static Map<UUID, Historyjao> cache = new HashMap<>();

    OfflinePlayer player;
    String name;
    UUID uuid;
    boolean found = false;
    boolean notify = true;
    List<Data> data = new ArrayList<>();

    long DBSyncTime = -1L;

    private Historyjao(OfflinePlayer offplayer) {
        this.player = offplayer;
        DBSync();
    }

    public static Historyjao getHistoryjao(OfflinePlayer offplayer) {
        Historyjao hist = new Historyjao(offplayer);
        if (cache.containsKey(offplayer.getUniqueId())) {
            hist = cache.get(offplayer.getUniqueId());
        }
        hist.DBSync();
        return hist;
    }

    public boolean add(String message) {
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO jaoHistory (player, uuid, message, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP);")) {
                statement.setString(1, player.getName());
                statement.setString(2, player.getUniqueId().toString());
                statement.setString(3, message);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        DBSync(true);
        return true;
    }

    public boolean autoAdd(String prefix, String details) {
        if (getDataList().stream().anyMatch(d -> d.message.startsWith(prefix))) {
            return false;
        }
        return add(prefix + " " + details);
    }

    public boolean disable(int id) {
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE jaoHistory SET disabled = ? WHERE uuid = ? AND id = ? ORDER BY id DESC")) {
                statement.setBoolean(1, true);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        DBSync(true);

        return true;
    }

    public boolean setNotify(int id, boolean changeTo) {
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE jaoHistory SET notify = ? WHERE uuid = ? AND id = ? ORDER BY id DESC")) {
                statement.setBoolean(1, changeTo);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        DBSync(true);

        return true;
    }

    public void DBSync() {
        DBSync(false);
    }

    public void DBSync(boolean force) {
        if (!force && ((DBSyncTime + 30 * 60 * 1000) > System.currentTimeMillis())) {
            return; // 30分未経過
        }
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn
                .prepareStatement("SELECT * FROM jaoHistory WHERE uuid = ?")) {
                statement.setString(1, player.getUniqueId().toString());
                try (ResultSet res = statement.executeQuery()) {
                    this.data.clear();
                    while (res.next()) {
                        this.name = res.getString("player");
                        this.uuid = UUID.fromString(res.getString("uuid"));

                        if (res.getBoolean("disabled")) {
                            continue;
                        }

                        Data d = new Data();
                        d.id = res.getInt("id");
                        d.player = res.getString("player");
                        d.message = res.getString("message");
                        d.disabled = res.getBoolean("disabled");
                        d.notify = res.getBoolean("notify");
                        d.created_at = res.getTimestamp("created_at").getTime() / 1000;
                        d.updated_at = res.getTimestamp("updated_at").getTime() / 1000;
                        this.data.add(d);

                        this.found = true;
                    }
                }
            }

            DBSyncTime = System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();

            this.name = player.getName();
            this.uuid = player.getUniqueId();
            this.found = false;
        }
        cache.put(player.getUniqueId(), this);
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<Data> getDataList() {
        return data;
    }

    public long getDBSyncTime() {
        return DBSyncTime;
    }

    public boolean isFound() {
        return found;
    }

    public boolean isNotify() {
        return notify;
    }

    public static class Data {
        public int id;
        public String player;
        public String message;
        public boolean disabled;
        public boolean notify;
        public long created_at;
        public long updated_at;

        public Date getCreatedAt() {
            return new Date(created_at * 1000);
        }

        public Date getUpdatedAt() {
            return new Date(updated_at * 1000);
        }
    }
}
