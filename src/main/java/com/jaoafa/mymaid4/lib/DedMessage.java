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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DedMessage {
    static final Set<Details> cache = new HashSet<>();
    static long DBSyncTime = -1L;

    /**
     * DedMessageを追加します。
     *
     * @param details 追加するDedMessageのDetails
     *
     * @return 追加できたかどうか
     */
    public static boolean add(Player player, Details details) {
        DBSync(false);
        if (!MyMaidData.isMainDBActive()) {
            throw new IllegalStateException("Main.isMainDBActive == false");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO dedmessage (mcid, uuid, message, world, x1, y1, z1, x2, y2, z2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                statement.setString(1, player.getName());
                statement.setString(2, player.getUniqueId().toString());
                statement.setString(3, details.getMessage());
                statement.setString(4, details.getWorld().getName());
                statement.setInt(5, details.getFirstLocation().getBlockX());
                statement.setInt(6, details.getFirstLocation().getBlockY());
                statement.setInt(7, details.getFirstLocation().getBlockZ());
                statement.setInt(8, details.getSecondLocation().getBlockX());
                statement.setInt(9, details.getSecondLocation().getBlockY());
                statement.setInt(10, details.getSecondLocation().getBlockZ());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(DedMessage.class, e);
            return false;
        }
        DBSync(true);
        return true;
    }

    /**
     * DedMessageを取得します。
     *
     * @param id 取得するDedMessageのID
     *
     * @return DedMessage.Details
     */
    @Nullable
    public static Details get(int id) {
        DBSync(false);
        return cache.stream().filter(d -> d.getId() == id).findAny().orElse(null);
    }

    /**
     * DedMessageを削除します。追加者本人と運営のみ削除できます。
     *
     * @param id 削除するDedMessageのID
     *
     * @return 削除できたかどうか
     */
    public static boolean remove(Player player, int id) {
        DBSync(false);
        if (!MyMaidData.isMainDBActive()) {
            throw new IllegalStateException("Main.isMainDBActive == false");
        }

        DedMessage.Details details = DedMessage.get(id);
        if (details == null) {
            return false;
        }
        if (details.getUUID() != player.getUniqueId() && !MyMaidLibrary.isAM(player)) {
            return false;
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM dedmessage WHERE id = ?")) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(DedMessage.class, e);
            return false;
        }
        DBSync(true);
        return true;
    }

    /**
     * 指定されたVectorを囲むDedMessage.Detailsのひとつを返します。
     *
     * @param vector Vector
     *
     * @return 一番最初に見つかったDedMessage.Details。なければnull
     */
    @Nullable
    public static Details match(Vector vector) {
        DBSync(false);
        for (Details details : cache) {
            if (vector.isInAABB(Vector.getMinimum(
                details.getFirstLocation(),
                details.getSecondLocation()
            ), Vector.getMaximum(
                details.getFirstLocation(),
                details.getSecondLocation()
            ).add(new Vector(1, 1, 1)))) {
                return details;
            }
        }
        return null;
    }

    /**
     * DedMessageをすべて取得します。
     *
     * @return リストのDedMessage.Details
     */
    public static Set<Details> getAll() {
        DBSync(false);
        return cache;
    }

    public static void DBSync(boolean force) {
        if (!force && ((DBSyncTime + 30 * 60 * 1000) > System.currentTimeMillis())) {
            return; // 30分未経過
        }
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM dedmessage")) {
                try (ResultSet res = statement.executeQuery()) {
                    cache.clear();
                    while (res.next()) {
                        Details d = new Details(
                            res.getInt("id"),
                            res.getString("mcid"),
                            UUID.fromString(res.getString("uuid")),
                            res.getString("message"),
                            Bukkit.getWorld(res.getString("world")),
                            new Vector(
                                res.getInt("x1"),
                                res.getInt("y1"),
                                res.getInt("z1")
                            ),
                            new Vector(
                                res.getInt("x2"),
                                res.getInt("y2"),
                                res.getInt("z2")
                            ),
                            res.getTimestamp("created_at")
                        );
                        cache.add(d);
                    }
                }
            }

            DBSyncTime = System.currentTimeMillis();
        } catch (SQLException e) {
            MyMaidLibrary.reportError(DedMessage.class, e);
        }
    }

    public static class Details {
        private final String mcid;
        private final UUID uuid;
        private final String message;
        private final World world;
        private final Vector firstLocation;
        private final Vector secondLocation;
        private int id = -1;
        private Timestamp created_at = null;

        public Details(Player player, String message, World world, Vector firstLocation, Vector secondLocation) {
            this.mcid = player.getName();
            this.uuid = player.getUniqueId();
            this.message = message;
            this.world = world;
            this.firstLocation = firstLocation;
            this.secondLocation = secondLocation;
        }

        private Details(int id, String mcid, UUID uuid, String message, World world, Vector firstLocation, Vector secondLocation, Timestamp created_at) {
            this.id = id;
            this.mcid = mcid;
            this.uuid = uuid;
            this.message = message;
            this.world = world;
            this.firstLocation = firstLocation;
            this.secondLocation = secondLocation;
            this.created_at = created_at;
        }

        public int getId() {
            return id;
        }

        public String getMinecraftID() {
            return mcid;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getMessage() {
            return message;
        }

        public World getWorld() {
            return world;
        }

        public Vector getFirstLocation() {
            return firstLocation;
        }

        public Vector getSecondLocation() {
            return secondLocation;
        }

        public Timestamp getCreatedAt() {
            return created_at;
        }
    }
}
