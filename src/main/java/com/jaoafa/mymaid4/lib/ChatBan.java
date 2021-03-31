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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

/**
 * ChatBan Library
 */
public class ChatBan {
    /** ChatBan IdとChatBan情報の紐付け・キャッシュ */
    public static Map<Integer, ChatBanData> cacheData = new HashMap<>();
    /** プレイヤーとChatBan Idの紐付け */
    public static Map<UUID, Integer> linkChatBanData = new HashMap<>();

    OfflinePlayer player;
    ChatBanData chatbanData;

    public ChatBan(OfflinePlayer player) {
        this.player = player;
        if (linkChatBanData.containsKey(player.getUniqueId()) && cacheData.containsKey(linkChatBanData.get(player.getUniqueId()))) {
            chatbanData = cacheData.get(linkChatBanData.get(player.getUniqueId()));
        } else {
            chatbanData = new ChatBanData(player);
        }
        chatbanData.fetchData(false);
    }

    public static List<ChatBanData> getActiveChatBans() {
        List<ChatBanData> chatbans = new ArrayList<>();

        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM chatban WHERE status = ?")) {
                stmt.setBoolean(1, true);

                ResultSet res = stmt.executeQuery();
                while (res.next()) {
                    chatbans.add(new ChatBanData(
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

                return chatbans;
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(ChatBan.class, e);
            return null;
        }
    }

    /**
     * このユーザーをChatBanに追加します。
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
                "INSERT INTO chatban (player, uuid, banned_by, reason, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)")) {
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
                    Component.text("[ChatBan]"),
                    Component.space(),
                    Component.text("プレイヤー「", NamedTextColor.GREEN),
                    Component.text(displayName, NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showEntity(Key.key("player"), player.getUniqueId(), Component.text(displayName))),
                    Component.text("」が「", NamedTextColor.GREEN),
                    Component.text(reason, NamedTextColor.GREEN),
                    Component.text("」という理由でChatBanされました。", NamedTextColor.GREEN)
                ));

                TextChannel sendTo = getDiscordSendTo();
                sendTo.sendMessage(
                    String.format("__**ChatBan[追加]**__: プレイヤー「%s」が「%s」によって「%s」という理由でChatBanされました。",
                        MyMaidLibrary.DiscordEscape(player.getName()),
                        MyMaidLibrary.DiscordEscape(banned_by),
                        MyMaidLibrary.DiscordEscape(reason))).queue();
                if (MyMaidData.getServerChatChannel() != null) {
                    MyMaidData.getServerChatChannel().sendMessage(
                        String.format("__**ChatBan[追加]**__: プレイヤー「%s」が「%s」によって「%s」という理由でChatBanされました。",
                            MyMaidLibrary.DiscordEscape(player.getName()),
                            MyMaidLibrary.DiscordEscape(banned_by),
                            MyMaidLibrary.DiscordEscape(reason))).queue();
                }

                chatbanData.id = -1;
                chatbanData.fetchData(true);
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
        return chatbanData.isStatus();
    }

    /**
     * このユーザーのChatBanを解除します。
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
                "UPDATE chatban SET status = ?, remover = ? WHERE uuid = ? ORDER BY id DESC LIMIT 1")) {
                stmt.setBoolean(1, false);
                stmt.setString(2, remover);
                stmt.setString(3, player.getUniqueId().toString());
                boolean isSuccess = stmt.executeUpdate() == 1;
                if (!isSuccess) {
                    return Result.UNKNOWN_ERROR;
                }

                String displayName = player.getName() != null ? player.getName() : player.getUniqueId().toString();
                Bukkit.getServer().sendMessage(Component.text().append(
                    Component.text("[ChatBan]"),
                    Component.space(),
                    Component.text("プレイヤー「", NamedTextColor.GREEN),
                    Component.text(displayName, NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showEntity(Key.key("player"), player.getUniqueId(), Component.text(displayName))),
                    Component.text("」のChatBanを解除しました。", NamedTextColor.GREEN)
                ));

                TextChannel sendTo = getDiscordSendTo();
                sendTo.sendMessage(
                    String.format("__**ChatBan[解除]**__: プレイヤー「%s」のChatBanを「%s」によって解除されました。",
                        MyMaidLibrary.DiscordEscape(player.getName()), MyMaidLibrary.DiscordEscape(remover)))
                    .queue();
                if (MyMaidData.getServerChatChannel() != null) {
                    MyMaidData.getServerChatChannel().sendMessage(
                        String.format("__**ChatBan[解除]**__: プレイヤー「%s」のChatBanを「%s」によって解除されました。",
                            MyMaidLibrary.DiscordEscape(player.getName()), MyMaidLibrary.DiscordEscape(remover))).queue();
                }
                chatbanData.id = -1;
                chatbanData.fetchData(true);
                return Result.SUCCESS;
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return Result.DATABASE_ERROR;
        }
    }

    /**
     * 発言したメッセージをデータベースに記録します。
     *
     * @param message 発言したメッセージ
     *
     * @return Result
     */
    public Result addMessageDB(String message) {
        if (!MyMaidData.isMainDBActive()) {
            return Result.DATABASE_NOT_ACTIVE;
        }
        if (isBanned()) {
            return Result.ALREADY;
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
            PreparedStatement stmt = conn
                .prepareStatement("INSERT INTO chatjailmsg (player, uuid, message) VALUES (?, ?, ?);");
            stmt.setString(1, player.getName());
            stmt.setString(2, player.getUniqueId().toString());
            stmt.setString(3, message);
            boolean isSuccess = stmt.executeUpdate() == 1;
            if (!isSuccess) {
                return Result.UNKNOWN_ERROR;
            }

            return Result.SUCCESS;
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return Result.DATABASE_ERROR;
        }
    }

    /**
     * ChatBanDataを返します
     *
     * @return ChatBanData
     */
    public ChatBanData getChatBanData() {
        return chatbanData;
    }

    /**
     * 各種ChatBan通知の送信先を返します。
     *
     * @return 送信先
     */
    TextChannel getDiscordSendTo() {
        JDA jda = Main.getMyMaidConfig().getJDA();
        if (jda == null) {
            return null;
        }
        if (MyMaidLibrary.isAMR(player)) {
            return jda.getTextChannelById(690854369783971881L); // #rma_chatban
        } else {
            return jda.getTextChannelById(709399145575874690L); // #chatban
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

    public static class ChatBanData {
        /** ChatBan Id */
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

        /** 空のChatBanデータを作成します。 */
        private ChatBanData() {
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param id ChatBan Id
         */
        private ChatBanData(int id) {
            this.id = id;
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param player プレイヤー
         */
        private ChatBanData(OfflinePlayer player) {
            this.playerUUID = player.getUniqueId();
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param player    プレイヤー
         * @param banned_by 処罰者
         * @param reason    処罰理由
         */
        public ChatBanData(OfflinePlayer player, String banned_by, String reason) {
            this.playerName = player.getName();
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param player     プレイヤー
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public ChatBanData(OfflinePlayer player, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param playerName プレイヤー名
         * @param playerUUID プレイヤーUUID
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public ChatBanData(String playerName, UUID playerUUID, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.banned_by = banned_by;
            this.reason = reason;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }

        /**
         * 指定された情報でChatBanデータを作成します。
         *
         * @param id         ChatBan Id
         * @param playerName プレイヤー名
         * @param playerUUID プレイヤーUUID
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public ChatBanData(int id, String playerName, UUID playerUUID, String banned_by, String reason, String remover, boolean status, Timestamp created_at) {
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
         * ChatBanIdを取得します。-1の場合データが存在しないか、フェッチされていません。
         *
         * @return ChatBanId
         */
        public int getChatBanId() {
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
                    stmt = conn.prepareStatement("SELECT * FROM chatban WHERE id = ?");
                    stmt.setInt(1, id);
                } else if (playerName != null) {
                    stmt = conn.prepareStatement("SELECT * FROM chatban WHERE player = ? ORDER BY id DESC LIMIT 1");
                    stmt.setString(1, playerName);
                } else if (playerUUID != null) {
                    stmt = conn.prepareStatement("SELECT * FROM chatban WHERE uuid = ? ORDER BY id DESC LIMIT 1");
                    stmt.setString(1, playerUUID.toString());
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
                    linkChatBanData.put(UUID.fromString(res.getString("uuid")), id);
                }

                return FetchDataResult.SUCCESS;
            } catch (SQLException e) {
                MyMaidLibrary.reportError(getClass(), e);
                return FetchDataResult.DATABASE_ERROR;
            }
        }
    }
}
