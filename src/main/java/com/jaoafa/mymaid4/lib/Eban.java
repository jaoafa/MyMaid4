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

import com.jaoafa.mymaid4.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

/**
 * Eban Library
 */
public class Eban {
    /** Eban IdとEban情報の紐付け・キャッシュ */
    public static Map<Integer, EbanData> cacheData = new HashMap<>();
    /** プレイヤーとEban Idの紐付け */
    public static Map<UUID, Integer> linkEbanData = new HashMap<>();

    OfflinePlayer player;
    EbanData ebanData;

    public Eban(OfflinePlayer player) {
        this.player = player;
        if (linkEbanData.containsKey(player.getUniqueId()) && cacheData.containsKey(linkEbanData.get(player.getUniqueId()))) {
            ebanData = cacheData.get(linkEbanData.get(player.getUniqueId()));
        } else {
            ebanData = new EbanData(player);
        }
        ebanData.fetchData(false);

        Map<UUID, Integer> tempLinkEbanData = linkEbanData;
        EbanData tempEbanData = ebanData;
        if (!ebanData.isStatus()) {
            linkEbanData.entrySet().stream()
                .filter(entry -> entry.getValue() == tempEbanData.getEbanId())
                .forEach(entry -> tempLinkEbanData.remove(entry.getKey()));
        }
        ebanData = tempEbanData;
        linkEbanData = tempLinkEbanData;
    }

    public static List<EbanData> getActiveEbans() {
        List<EbanData> ebans = new ArrayList<>();

        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM eban_new WHERE status = ?")) {
                stmt.setBoolean(1, true);

                ResultSet res = stmt.executeQuery();
                while (res.next()) {
                    ebans.add(new EbanData(
                        res.getInt("id"),
                        res.getString("player"),
                        UUID.fromString(res.getString("uuid")),
                        res.getString("banned_by"),
                        res.getString("reason"),
                        res.getString("remover"),
                        res.getBoolean("status"),
                        res.getTimestamp("created_at")
                    ));
                }

                return ebans;
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(Eban.class, e);
            return null;
        }
    }

    /**
     * このユーザーをEbanに追加します。
     *
     * @param banned_by Banを実行した実行者情報
     * @param reason    理由
     *
     * @return Result
     */
    public Result addBan(String banned_by, String reason) {
        if (!MyMaidData.isMainDBActive()) {
            return Result.DATABASE_NOT_ACTIVE;
        }
        if (isBanned()) {
            return Result.ALREADY;
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO eban_new (player, uuid, banned_by, reason, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
                stmt.setString(1, player.getName());
                stmt.setString(2, player.getUniqueId().toString());
                stmt.setString(3, banned_by);
                stmt.setString(4, reason);
                boolean isSuccess = stmt.executeUpdate() == 1;
                if (!isSuccess) {
                    return Result.UNKNOWN_ERROR;
                }

                String displayName = player.getName() != null ? player.getName() : player.getUniqueId().toString();
                Bukkit.getServer().sendMessage(Component.text().append(
                    Component.text("[Eban]"),
                    Component.space(),
                    Component.text("プレイヤー「", NamedTextColor.GREEN),
                    Component.text(displayName, NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showEntity(Key.key("player"), player.getUniqueId(), Component.text(displayName))),
                    Component.text("」が「", NamedTextColor.GREEN),
                    Component.text(reason, NamedTextColor.GREEN),
                    Component.text("」という理由でEbanされました。", NamedTextColor.GREEN)
                ));

                TextChannel sendTo = getDiscordSendTo();
                sendTo.sendMessage(
                    String.format("__**Eban[追加]**__: プレイヤー「%s」が「%s」によって「%s」という理由でEbanされました。",
                        MyMaidLibrary.DiscordEscape(player.getName()),
                        MyMaidLibrary.DiscordEscape(banned_by),
                        MyMaidLibrary.DiscordEscape(reason))).queue();
                if (MyMaidData.getServerChatChannel() != null) {
                    MyMaidData.getServerChatChannel().sendMessage(
                        String.format("__**Eban[追加]**__: プレイヤー「%s」が「%s」によって「%s」という理由でEbanされました。",
                            MyMaidLibrary.DiscordEscape(player.getName()),
                            MyMaidLibrary.DiscordEscape(banned_by),
                            MyMaidLibrary.DiscordEscape(reason))).queue();
                }

                if (player.isOnline() && player.getPlayer() != null) {
                    if (player.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                        player.getPlayer().setGameMode(GameMode.CREATIVE);
                    }

                    World Jao_Afa = Bukkit.getServer().getWorld("Jao_Afa");
                    Location minami = new Location(Jao_Afa, 2856, 69, 2888);
                    player.getPlayer().teleport(minami);
                }

                ebanData.fetchData(true);
                return Result.SUCCESS;
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return Result.DATABASE_ERROR;
        }
    }

    /**
     * このユーザーが処罰済みかどうか調べます
     *
     * @return 処罰済みかどうか
     */
    public boolean isBanned() {
        return ebanData.isStatus();
    }

    /**
     * このユーザーのEbanを解除します。
     *
     * @param remover 解除者
     *
     * @return Result
     */
    public Result removeBan(String remover) {
        if (!MyMaidData.isMainDBActive()) {
            return Result.DATABASE_NOT_ACTIVE;
        }
        if (!isBanned()) {
            return Result.ALREADY;
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE eban_new SET status = ? AND remover = ? WHERE uuid = ? ORDER BY id DESC LIMIT 1")) {
                stmt.setBoolean(1, false);
                stmt.setString(2, remover);
                stmt.setString(3, player.getUniqueId().toString());
                boolean isSuccess = stmt.executeUpdate() == 1;
                if (!isSuccess) {
                    return Result.UNKNOWN_ERROR;
                }

                String displayName = player.getName() != null ? player.getName() : player.getUniqueId().toString();
                Bukkit.getServer().sendMessage(Component.text().append(
                    Component.text("[Eban]"),
                    Component.space(),
                    Component.text("プレイヤー「", NamedTextColor.GREEN),
                    Component.text(displayName, NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showEntity(Key.key("player"), player.getUniqueId(), Component.text(displayName))),
                    Component.text("」のEbanを解除しました。", NamedTextColor.GREEN)
                ));

                TextChannel sendTo = getDiscordSendTo();
                sendTo.sendMessage(
                    String.format("__**Eban[解除]**__: プレイヤー「%s」のEbanを「%s」によって解除されました。",
                        MyMaidLibrary.DiscordEscape(player.getName()), MyMaidLibrary.DiscordEscape(remover)))
                    .queue();
                if (MyMaidData.getServerChatChannel() != null) {
                    MyMaidData.getServerChatChannel().sendMessage(
                        String.format("__**Eban[解除]**__: プレイヤー「%s」のEbanを「%s」によって解除されました。",
                            MyMaidLibrary.DiscordEscape(player.getName()), MyMaidLibrary.DiscordEscape(remover))).queue();
                }

                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(Component.text().append(
                        Component.text("[Eban]"),
                        Component.space(),
                        Component.text("元の場所に戻るために", NamedTextColor.GREEN),
                        Component.space(),
                        Component.text("/untp", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("/untpを実行します。")))
                            .clickEvent(ClickEvent.runCommand("/untp")),
                        Component.space(),
                        Component.text("が使えるかもしれません。", NamedTextColor.GREEN)
                    ));
                }

                ebanData.fetchData(true);
                return Result.SUCCESS;
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return Result.DATABASE_ERROR;
        }
    }

    /**
     * EbanDataを返します
     *
     * @return EbanData
     */
    public EbanData getEbanData() {
        return ebanData;
    }

    /**
     * 各種Eban通知の送信先を返します。
     *
     * @return 送信先
     */
    TextChannel getDiscordSendTo() {
        JDA jda = Main.getMyMaidConfig().getJDA();
        if (jda == null) {
            return null;
        }
        if (MyMaidLibrary.isAMR(player)) {
            return jda.getTextChannelById(690854369783971881L); // #rma_eban
        } else {
            return jda.getTextChannelById(709399145575874690L); // #eban
        }
    }

    public enum Result {
        /** 成功 */
        SUCCESS,
        /** 既に処理済 */
        ALREADY,
        /** データベースが無効または接続不能 */
        DATABASE_NOT_ACTIVE,
        /** データベース通信時にエラー */
        DATABASE_ERROR,
        /** 不明なエラー */
        UNKNOWN_ERROR
    }

    enum FetchDataResult {
        /** 成功 */
        SUCCESS,
        /** データなし */
        NOTFOUND,
        /** データベースが無効または接続不能 */
        DATABASE_NOT_ACTIVE,
        /** データベース通信時にエラー */
        DATABASE_ERROR,
        /** キャッシュから取得 */
        CACHED,
        /** 不明なエラー */
        UNKNOWN
    }

    public static class EbanData {
        /** Eban Id */
        private int id = -1;
        /** 処罰対象プレイヤー名 */
        private String playerName = null;
        /** 処罰対象プレイヤーUUID */
        private UUID playerUUID = null;
        /** 処罰者 */
        private String banned_by = null;
        /** 理由 */
        private String reason = null;
        /** 解除者 */
        private String remover = null;
        /** 処罰中か */
        private boolean status = false;
        /** データ作成時刻 */
        private Timestamp created_at = null;
        /** フェッチ日時 */
        private long dbSyncedTime = -1L;

        /** 空のEbanデータを作成します。 */
        private EbanData() {
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param id Eban Id
         */
        private EbanData(int id) {
            this.id = id;
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param player プレイヤー
         */
        private EbanData(OfflinePlayer player) {
            this.playerUUID = player.getUniqueId();
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param player    プレイヤー
         * @param banned_by 処罰者
         * @param reason    処罰理由
         */
        public EbanData(OfflinePlayer player, String banned_by, String reason) {
            this.playerName = player.getName();
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param player     プレイヤー
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public EbanData(OfflinePlayer player, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param playerName プレイヤー名
         * @param playerUUID プレイヤーUUID
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public EbanData(String playerName, UUID playerUUID, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.banned_by = banned_by;
            this.reason = reason;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }

        /**
         * 指定された情報でEbanデータを作成します。
         *
         * @param id         Eban Id
         * @param playerName プレイヤー名
         * @param playerUUID プレイヤーUUID
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public EbanData(int id, String playerName, UUID playerUUID, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
            this.id = id;
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.banned_by = banned_by;
            this.reason = reason;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }


        /**
         * EbanIdを取得します。-1の場合データが存在しないか、フェッチされていません。
         *
         * @return EbanId
         */
        public int getEbanId() {
            return id;
        }

        /**
         * プレイヤーを取得します。
         *
         * @return プレイヤー
         */
        @Nullable
        public OfflinePlayer getPlayer() {
            return playerUUID != null ? Bukkit.getOfflinePlayer(playerUUID) : null;
        }

        /**
         * プレイヤー名を取得します。
         *
         * @return プレイヤー名
         */
        @Nullable
        public String getPlayerName() {
            return playerName;
        }

        /**
         * プレイヤーUUIDを取得します
         *
         * @return プレイヤーUUID
         */
        @Nullable
        public UUID getPlayerUUID() {
            return playerUUID;
        }

        /**
         * 処罰者名を取得します。
         *
         * @return 処罰者名
         */
        @Nullable
        public String getBannedBy() {
            return banned_by;
        }

        /**
         * 処罰理由を取得します。
         *
         * @return 処罰理由
         */
        @Nullable
        public String getReason() {
            return reason;
        }

        /**
         * 解除者名を取得します。
         *
         * @return 解除者名
         */
        @Nullable
        public String getRemover() {
            return remover;
        }

        /**
         * 現在の状態を取得します。
         *
         * @return 現在の状態。Trueの場合処罰中。
         */
        public boolean isStatus() {
            return status;
        }

        /**
         * データ作成日時(処罰日時)を取得します。
         *
         * @return データ作成日時
         */
        @Nullable
        public Timestamp getCreatedAt() {
            return created_at;
        }

        /**
         * データをフェッチします。forceがFalseの場合、最終情報取得から1時間経過していない場合キャッシュ情報を利用します。
         *
         * @param force 強制的にフェッチするか
         *
         * @return FetchDataResult
         */
        public FetchDataResult fetchData(boolean force) {
            if (!force && ((dbSyncedTime + 60 * 60 * 1000) > System.currentTimeMillis())) {
                return FetchDataResult.CACHED; // 30分未経過
            }
            if (!MyMaidData.isMainDBActive()) {
                return FetchDataResult.DATABASE_NOT_ACTIVE;
            }
            try {
                Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();

                // このへんの処理綺麗に書きたい
                PreparedStatement stmt;
                if (id != -1) {
                    stmt = conn.prepareStatement("SELECT * FROM eban_new WHERE id = ?");
                    stmt.setInt(1, id);
                } else if (playerName != null) {
                    stmt = conn.prepareStatement("SELECT * FROM eban_new WHERE player = ? AND status = ? ORDER BY id DESC LIMIT 1");
                    stmt.setString(1, playerName);
                    stmt.setBoolean(2, true);
                } else if (playerUUID != null) {
                    stmt = conn.prepareStatement("SELECT * FROM eban_new WHERE uuid = ? AND status = ? ORDER BY id DESC LIMIT 1");
                    stmt.setString(1, playerUUID.toString());
                    stmt.setBoolean(2, true);
                } else {
                    throw new IllegalStateException("データをフェッチするために必要な情報が足りません。");
                }

                try (ResultSet res = stmt.executeQuery()) {
                    if (!res.next()) {
                        return FetchDataResult.NOTFOUND;
                    }

                    this.id = res.getInt("id");
                    this.playerName = res.getString("player");
                    this.playerUUID = UUID.fromString(res.getString("uuid"));
                    this.banned_by = res.getString("banned_by");
                    this.reason = res.getString("reason");
                    this.remover = res.getString("remover");
                    this.status = res.getBoolean("status");
                    this.created_at = res.getTimestamp("created_at");
                    this.dbSyncedTime = System.currentTimeMillis();

                    cacheData.put(id, this);
                    linkEbanData.put(UUID.fromString(res.getString("uuid")), id);
                }

                return FetchDataResult.SUCCESS;
            } catch (SQLException e) {
                MyMaidLibrary.reportError(getClass(), e);
                return FetchDataResult.DATABASE_ERROR;
            }
        }
    }
}
