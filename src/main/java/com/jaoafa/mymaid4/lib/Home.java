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

import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Home extends MyMaidLibrary {
    static final Map<UUID, Set<Detail>> homeNames = new HashMap<>();
    final OfflinePlayer player;

    public Home(OfflinePlayer player) {
        this.player = player;
    }

    /**
     * 登録されているホームの一覧をフェッチしてキャッシュする
     *
     * @param player 対象のプレイヤー
     */
    static void fetchHomeNames(OfflinePlayer player) {
        Set<Detail> list = new HashSet<>();
        if (!MyMaidData.isMainDBActive()) {
            return;
        }
        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM home WHERE uuid = ?");
            stmt.setString(1, player.getUniqueId().toString());
            try (ResultSet res = stmt.executeQuery()) {
                while (res.next()) {
                    list.add(new Detail(
                        res.getString("name"),
                        res.getString("world"),
                        res.getDouble("x"),
                        res.getDouble("y"),
                        res.getDouble("z"),
                        res.getFloat("yaw"),
                        res.getFloat("pitch"),
                        res.getTimestamp("create_at")
                    ));
                }
            }
            homeNames.put(player.getUniqueId(), list);
        } catch (SQLException e) {
            reportError(Home.class, e);
        }
    }

    public static List<String> suggestHomeName(final CommandContext<CommandSender> context, final String current) {
        List<String> list = new ArrayList<>();
        if (!(context.getSender() instanceof Player player)) {
            return list;
        }
        if (!homeNames.containsKey(player.getUniqueId())) {
            fetchHomeNames(player);
        }
        list.addAll(homeNames.get(player.getUniqueId()).stream().map(detail -> detail.name).toList());

        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * ホーム情報を取得する
     *
     * @param name ホームネーム
     *
     * @return ホームの場所。なければNull
     */
    public Detail get(String name) {
        if (!MyMaidData.isMainDBActive()) {
            return null;
        }
        Set<Detail> homes = getHomes();
        return homes.stream()
            .filter(_detail -> _detail.name.equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * ホームを設定する
     *
     * @param name ホームネーム
     * @param loc  ホームの場所
     *
     * @return 成功したか
     */
    public boolean set(String name, Location loc) {
        if (!MyMaidData.isMainDBActive()) {
            return false;
        }
        if (exists(name)) {
            return false;
        }

        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO home (player, uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
                stmt.setString(1, player.getName()); // player
                stmt.setString(2, player.getUniqueId().toString()); // uuid
                stmt.setString(3, name); // name
                stmt.setString(4, loc.getWorld().getName()); // world
                stmt.setDouble(5, loc.getX()); // x
                stmt.setDouble(6, loc.getY()); // y
                stmt.setDouble(7, loc.getZ()); // z
                stmt.setFloat(8, loc.getYaw()); // yaw
                stmt.setFloat(9, loc.getPitch()); // pitch
                boolean bool = stmt.executeUpdate() != 0;

                fetchHomeNames(player);

                return bool;
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            return false;
        }
    }

    /**
     * ホームを削除する
     *
     * @param name ホームネーム
     *
     * @return 成功したか
     */
    public boolean remove(String name) {
        if (!MyMaidData.isMainDBActive()) {
            return false;
        }
        if (!exists(name)) {
            return false;
        }

        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM home WHERE name = ? AND uuid = ?")) {
                stmt.setString(1, name); // player
                stmt.setString(2, player.getUniqueId().toString()); // uuid
                boolean bool = stmt.executeUpdate() != 0;

                fetchHomeNames(player);

                return bool;
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            return false;
        }
    }

    /**
     * ホームが存在するかどうかをチェックする
     *
     * @param name ホームネーム
     *
     * @return ホームが存在すればTrue
     */
    public boolean exists(String name) {
        if (!MyMaidData.isMainDBActive()) {
            return false;
        }
        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        try {
            Connection conn = manager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM home WHERE name = ? AND uuid = ? LIMIT 1");
            stmt.setString(1, name);
            stmt.setString(2, player.getUniqueId().toString());
            try (ResultSet res = stmt.executeQuery()) {
                return res.next();
            }
        } catch (SQLException e) {
            reportError(getClass(), e);
            return false;
        }
    }

    /**
     * ホーム一覧を取得する
     *
     * @return ホーム一覧
     */
    public Set<Detail> getHomes() {
        if (!homeNames.containsKey(player.getUniqueId())) {
            fetchHomeNames(player);
        }
        return homeNames.get(player.getUniqueId());
    }

    public record Detail(String name, String worldName, double x, double y, double z, float yaw,
                         float pitch, Timestamp create_at) {
        public Location getLocation() {
            return new Location(
                Bukkit.getWorld(worldName),
                x,
                y,
                z,
                yaw,
                pitch
            );
        }

        public String getDate() {
            return MyMaidLibrary.sdfFormat(create_at);
        }
    }
}
