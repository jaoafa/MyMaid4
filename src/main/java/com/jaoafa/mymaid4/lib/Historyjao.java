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

import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.*;

public class Historyjao {
    static final Map<UUID, Historyjao> cache = new HashMap<>();

    final OfflinePlayer player;
    final List<Data> data = new ArrayList<>();
    String name;
    UUID uuid;
    boolean found = false;
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

    public boolean add(String message, CommandSender sender) {
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
            MyMaidLibrary.reportError(getClass(), e);
            return false;
        }
        addDiscordNotify(message, sender);
        DBSync(true);
        return true;
    }

    private void addDiscordNotify(String message, CommandSender sender) {
        JDA jda = Main.getMyMaidConfig().getJDA();
        if (jda == null) {
            return;
        }
        TextChannel channel = jda.getTextChannelById(862918445031882782L);
        if (channel == null) {
            return;
        }
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle("HISTORY ADDED")
            .setDescription("jaoHistoryに項目が追加されました。")
            .addField("MinecraftID", player.getName(), false)
            .addField("Message", message, false)
            .setTimestamp(Instant.now())
            .setColor(Color.YELLOW);
        if (sender instanceof Player) {
            builder.setAuthor(sender.getName(),
                "https://users.jaoafa.com/" + ((Player) sender).getUniqueId(),
                "https://crafatar.com/renders/head/" + ((Player) sender).getUniqueId() + "?overlay=true");
        }
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public boolean autoAdd(String prefix, String details) {
        if (getDataList().stream().anyMatch(d -> d.message.startsWith(prefix))) {
            return false;
        }
        return add(prefix + " " + details, null);
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
            MyMaidLibrary.reportError(getClass(), e);
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
                "UPDATE jaoHistory SET notify = ? WHERE uuid = ? AND id = ?")) {
                statement.setBoolean(1, changeTo);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return false;
        }
        DBSync(true);

        return true;
    }

    @Nullable
    public Date getWhenNotified() {
        return this.data.stream()
            .filter(d -> d.notified_at != null)
            .max(Comparator.comparingLong(d -> d.notified_at))
            .map(Data::getNotifiedAt)
            .orElse(null);
    }

    public boolean setNotified() {
        if (MyMaidData.getMainMySQLDBManager() == null) {
            throw new IllegalStateException("Main.MySQLDBManager == null");
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE jaoHistory SET notified_at = ? WHERE uuid = ? AND notify = ? AND disabled = ?")) {
                statement.setTimestamp(1, Timestamp.from(Instant.now()));
                statement.setString(2, player.getUniqueId().toString());
                statement.setBoolean(3, true);
                statement.setBoolean(4, false);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
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
                        d.notified_at = res.getTimestamp("notified_at") != null ? res.getTimestamp("notified_at").getTime() / 1000 : null;
                        d.created_at = res.getTimestamp("created_at").getTime() / 1000;
                        d.updated_at = res.getTimestamp("updated_at").getTime() / 1000;
                        this.data.add(d);

                        this.found = true;
                    }
                }
            }

            DBSyncTime = System.currentTimeMillis();
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);

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

    public static class Data {
        public int id;
        public String player;
        public String message;
        public boolean disabled;
        public boolean notify;
        public Long notified_at;
        public long created_at;
        public long updated_at;

        public Date getNotifiedAt() {
            return new Date(updated_at * 1000);
        }

        public Date getCreatedAt() {
            return new Date(created_at * 1000);
        }

        public Date getUpdatedAt() {
            return new Date(updated_at * 1000);
        }
    }
}
