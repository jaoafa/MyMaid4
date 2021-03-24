package com.jaoafa.mymaid4.lib;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jail Library
 */
public class Jail {
    /**
     * Jail IdとJail情報の紐付け・キャッシュ
     */
    static Map<Integer, JailData> cacheData = new HashMap<>();
    /**
     * プレイヤーとJail Idの紐付け
     */
    static Map<UUID, Integer> linkJailData = new HashMap<>();

    /**
     * このユーザーをJailに追加します。
     *
     * @param banned_by Banを実行した実行者情報
     * @param reason    理由
     * @return 成功したか
     */
    public boolean addBan(String banned_by, String reason) {
    }


    public boolean removeBan(String remover) {
    }

    public boolean isBanned() {
    }

    public boolean setTestment(String testment) {
    }

    /**
     * このユーザーの処罰時刻をDateで返します。
     *
     * @return
     */
    public Date getBanned() {
        DBSync();
        return new Date(banned_unixtime * 1000);
    }

    /**
     * 最後の処罰理由を取得します。
     *
     * @return 最後の処罰理由
     */
    public String getLastBanReason() {
        DBSync();
        return lastreason;
    }

    /**
     * 最後の処罰遺言を取得します。
     *
     * @return
     */
    public String getLastBanTestment() {
        DBSync();
        return lasttestment;
    }

    /**
     * 処罰を行ったユーザーを返します。
     *
     * @return 処罰を行ったユーザー
     */
    public String getBannedBy() {
        return banned_by;
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public long getDBSyncTime() {
        return DBSyncTime;
    }

    public TextChannel getDiscordSendTo() {
        try {
            String group;
            if (player.isOnline()) {
                group = PermissionsManager.getPermissionMainGroup(player.getPlayer());
            } else {
                group = PermissionsManager.getPermissionMainGroup(name);
            }

            if (group.equalsIgnoreCase("Regular") || group.equalsIgnoreCase("Moderator")
                || group.equalsIgnoreCase("Admin")) {

                return MyMaidConfig.getJDA().getTextChannelById(690854369783971881L); // #rma_jail
            } else {
                return MyMaidConfig.getJDA().getTextChannelById(709399145575874690L); // #jail
            }
        } catch (IllegalArgumentException e) {
            return MyMaidConfig.getJDA().getTextChannelById(709399145575874690L); // #jaotan
        }
    }

    enum FetchDataResult {
        /**
         * 成功時
         */
        SUCCESS(),
        /**
         * データなし
         */
        NOTFOUND(),
        /**
         * データベースが無効または接続不能
         */
        DATABASENOTACTIVE(),
        /**
         * データベース通信時にエラー
         */
        DATABASEERROR(),
        /**
         * キャッシュから取得
         */
        CACHED(),
        /**
         * 不明なエラー
         */
        UNKNOWN()
    }

    static class JailData {
        private final long dbSyncedTime = -1L;
        /**
         * Jail Id
         */
        private int id = -1;
        /**
         * 処罰対象プレイヤー名
         */
        private String playerName = null;
        /**
         * 処罰対象プレイヤーUUID
         */
        private UUID playerUUID = null;
        /**
         * 処罰者
         */
        private String banned_by = null;
        /**
         * 理由
         */
        private String reason = null;
        /**
         * 遺言
         */
        private String testment = null;
        /**
         * 解除者
         */
        private String remover = null;
        /**
         * 処罰中か
         */
        private boolean status = true;
        /**
         * データ作成時刻
         */
        private Timestamp created_at = null;

        /**
         * 空のJailデータを作成します。
         */
        private JailData() {
        }

        /**
         * 指定された情報でJailデータを作成します。
         *
         * @param id Jail Id
         */
        private JailData(int id) {
            this.id = id;
        }

        /**
         * 指定されたプレイヤーの最終Jail情報を取得します。
         *
         * @param player プレイヤー
         */
        public JailData(OfflinePlayer player) {
            this.playerUUID = player.getUniqueId();
        }

        /**
         * 指定された情報でJailデータを作成します。
         *
         * @param player    プレイヤー
         * @param banned_by 処罰者
         * @param reason    処罰理由
         */
        public JailData(OfflinePlayer player, String banned_by, String reason) {
            this.playerName = player.getName();
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
        }

        /**
         * 指定された情報でJailデータを作成します。
         *
         * @param player     プレイヤー
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param testment   遺言
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public JailData(OfflinePlayer player, String banned_by, String reason, String testment, String remover, boolean status, Timestamp created_at) {
            this.playerUUID = player.getUniqueId();
            this.banned_by = banned_by;
            this.reason = reason;
            this.testment = testment;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }

        /**
         * 指定された情報でJailデータを作成します。
         *
         * @param playerName プレイヤー名
         * @param playerUUID プレイヤーUUID
         * @param banned_by  　処罰者
         * @param reason     処罰理由
         * @param testment   遺言
         * @param remover    解除者
         * @param status     処罰中か
         * @param created_at データ作成時刻
         */
        public JailData(String playerName, UUID playerUUID, String banned_by, String reason, String testment, String remover, boolean status, Timestamp created_at) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.banned_by = banned_by;
            this.reason = reason;
            this.testment = testment;
            this.remover = remover;
            this.status = status;
            this.created_at = created_at;
        }


        /**
         * JailIdを取得します。-1の場合データが存在しないか、フェッチされていません。
         *
         * @return JailId
         */
        public int getJailId() {
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
        public String getBanned_by() {
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
         * 遺言を取得します。
         *
         * @return 遺言
         */
        @Nullable
        public String getTestment() {
            return testment;
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
         * @return FetchDataResult
         */
        public FetchDataResult fetchData(boolean force) {
            if (!force && ((dbSyncedTime + 60 * 60 * 1000) > System.currentTimeMillis())) {
                return FetchDataResult.CACHED; // 30分未経過
            }
            if (!MyMaidData.isMainDBActive()) {
                return FetchDataResult.DATABASENOTACTIVE;
            }
            try {
                Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();

                // このへんの処理綺麗に書きたい
                PreparedStatement stmt;
                if (id != -1) {
                    stmt = conn.prepareStatement("SELECT * FROM jail WHERE id = ?");
                    stmt.setInt(1, id);
                } else if (playerName != null) {
                    stmt = conn.prepareStatement("SELECT * FROM jail WHERE player = ? ORDER BY id LIMIT 1");
                    stmt.setString(1, playerName);
                } else if (playerUUID != null) {
                    stmt = conn.prepareStatement("SELECT * FROM jail WHERE uuid = ? ORDER BY id LIMIT 1");
                    stmt.setString(1, playerUUID.toString());
                } else {
                    throw new IllegalStateException("データをフェッチするために必要な情報が足りません。");
                }

                ResultSet res = stmt.executeQuery();
                if (!res.next()) {
                    return FetchDataResult.NOTFOUND;
                }

                this.playerName = res.getString("player");
                this.playerUUID = UUID.fromString(res.getString("uuid"));
                this.banned_by = res.getString("banned_by");
                this.reason = res.getString("reason");
                this.testment = res.getString("testment");
                this.remover = res.getString("remover");
                this.status = res.getBoolean("status");
                this.created_at = res.getTimestamp("created_at");

                return FetchDataResult.SUCCESS;
            } catch (SQLException e) {
                MyMaidLibrary.reportError(getClass(), e);
                return FetchDataResult.DATABASEERROR;
            }
        }
    }
}
