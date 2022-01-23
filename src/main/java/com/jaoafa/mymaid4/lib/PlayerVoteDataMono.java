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


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class PlayerVoteDataMono extends MyMaidLibrary {
    static final Map<UUID, PlayerVoteDataMono> cache = new HashMap<>();

    @NotNull
    final
    OfflinePlayer offplayer;
    boolean exists;
    int id = -1;
    int count = 0;
    boolean voted = false;
    long lastVotedTime = -1L;
    String customColor = null;
    private long dbSyncedTime = -1L;

    /**
     * 指定したオフラインプレイヤーの投票データを取得します。
     *
     * @param offplayer オフラインプレイヤー
     */
    public PlayerVoteDataMono(@NotNull OfflinePlayer offplayer) {
        this.offplayer = offplayer;

        changePlayerName();
        restoreCache();
        fetchData(false);
    }

    /**
     * 指定したプレイヤーネームの投票データを取得します。
     *
     * @param name プレイヤーネーム
     *
     * @deprecated プレイヤー名で検索するため、思い通りのプレイヤーを取得できない場合があります。
     */
    @Deprecated
    public PlayerVoteDataMono(@NotNull String name) {
        this.offplayer = Bukkit.getOfflinePlayer(name);

        changePlayerName();
        restoreCache();
        fetchData(false);
    }

    /**
     * その日のうち(前日or当日AM9:00～今)に誰も投票していないかどうか調べる（その日初めての投票かどうか）
     *
     * @return 誰も投票してなければtrue
     */
    public static boolean isTodayFirstVote() {
        // 仮
        try {
            MySQLDBManager sqlmanager = MyMaidData.getMainMySQLDBManager();
            Connection conn = sqlmanager.getConnection();
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM vote_monocraft")) {
                try (ResultSet res = statement.executeQuery()) {
                    while (res.next()) {
                        long last = res.getTimestamp("last").getTime() / 1000L;
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
                        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                        long today0 = cal.getTimeInMillis() / 1000L;

                        if (today0 < last && last < today0 + 86400) {
                            return false;
                        }
                    }
                }
            }
        } catch (UnsupportedOperationException | NullPointerException | NumberFormatException | SQLException e) {
            MyMaidLibrary.reportError(PlayerVoteDataMono.class, e);
            return false; // エラー発生したらその日の初めての投票ではないとみなす。ただしエラー通知はする
        }
        return true; // だれも投票してなかったら、trueを返す
    }

    void restoreCache() {
        if (cache.containsKey(offplayer.getUniqueId())) {
            PlayerVoteDataMono cached = cache.get(offplayer.getUniqueId());
            this.id = cached.id;
            this.count = cached.count;
            this.voted = cached.voted;
            this.lastVotedTime = cached.lastVotedTime;
            this.customColor = cached.customColor;
        }
    }

    /**
     * プレイヤーの投票数を取得します。
     *
     * @return プレイヤーの投票数
     */
    public int getVoteCount() {
        return count;
    }

    /**
     * プレイヤーの最終投票日時をunixtimeで取得します。
     *
     * @return プレイヤーの最終投票のunixtime
     */
    public long getLastVoteUnixTime() {
        return lastVotedTime;
    }

    public boolean isVoted() {
        try {
            long lasttime = getLastVoteUnixTime();
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            long today0 = cal.getTimeInMillis() / 1000L;

            cal.add(Calendar.DAY_OF_MONTH, -1);
            long yesterday0 = cal.getTimeInMillis() / 1000L;

            long now = System.currentTimeMillis() / 1000L;

            boolean checktype = today0 <= now; // true: 今日の0時 / false: 昨日の0時

            if (checktype) {
                if (lasttime < today0) {
                    return false;
                }
            } else {
                if (lasttime < yesterday0) {
                    return false;
                }
            }
        } catch (UnsupportedOperationException | NullPointerException | NumberFormatException e) {
            MyMaidLibrary.reportError(getClass(), e);
            return false; // エラー発生したら投票してないものとみなす
        }
        return true; // どれもひっかからなかったら投票したものとみなす
    }

    /**
     * プレイヤーの投票数データを作成する<br>
     * ※初めての投票時に作成すること！
     *
     * @return 作成できたかどうか
     *
     * @throws SQLException         内部でSQLExceptionが発生した場合
     * @throws NullPointerException 内部でNullPointerExceptionが発生した場合
     */
    public boolean create() throws SQLException, NullPointerException {
        if (exists())
            return false;
        MySQLDBManager sqlmanager = MyMaidData.getMainMySQLDBManager();
        Connection conn = sqlmanager.getConnection();
        try (PreparedStatement statement = conn.prepareStatement(
            "INSERT INTO vote_monocraft (player, uuid, count, first, last) VALUES (?, ?, ?, ?, ?);")) {
            statement.setString(1, offplayer.getName()); // player
            statement.setString(2, offplayer.getUniqueId().toString()); // uuid
            statement.setInt(3, 1);
            statement.setTimestamp(4, Timestamp.from(Instant.now()));
            statement.setTimestamp(5, Timestamp.from(Instant.now()));
            return statement.executeUpdate() != 0;
        }
    }

    /**
     * プレイヤーの投票数データが存在するかどうかを確認します。
     *
     * @return 存在するかどうか
     */
    public boolean exists() {
        return exists;
    }

    /**
     * プレイヤーの投票数に1つ追加します。
     *
     * @return 実行できたかどうか
     *
     * @throws SQLException 内部でSQLExceptionが発生した場合
     */
    public boolean add() throws SQLException {
        return add(System.currentTimeMillis() / 1000L);
    }

    /**
     * プレイヤーの投票数に1つ追加します。
     *
     * @param unixtime UnixTime
     *
     * @return 実行できたかどうか
     *
     * @throws SQLException 内部でSQLExceptionが発生した場合
     */
    public boolean add(long unixtime) throws SQLException {
        if (!exists()) {
            create();
            fetchData(true);
            return true;
        }
        int next = getVoteCount() + 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            return false;
        }
        Connection conn = MySQLDBManager.getConnection();
        try (PreparedStatement statement = conn
            .prepareStatement("UPDATE vote_monocraft SET count = ?, last = ? WHERE id = ?")) {
            statement.setInt(1, next);
            statement.setTimestamp(2, Timestamp.from(Instant.ofEpochSecond(unixtime)));
            statement.setInt(3, getID());
            int upcount = statement.executeUpdate();
            addLog(next);
            fetchData(true);
            return upcount != 0;
        }
    }

    private void addLog(int count) throws SQLException {
        MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
        if (MySQLDBManager == null) {
            throw new UnsupportedOperationException("We could not get the player.");
        }
        Connection conn = MySQLDBManager.getConnection();
        try (PreparedStatement statement = conn
            .prepareStatement("INSERT INTO votelog_success_mono (player, uuid, count, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP);")) {
            statement.setString(1, offplayer.getName());
            statement.setString(2, offplayer.getUniqueId().toString());
            statement.setInt(3, count);
            statement.executeUpdate();
        }
    }

    /**
     * プレイヤーのIDを取得します。
     *
     * @return プレイヤーのID
     */
    public int getID() {
        return id;
    }

    /**
     * プレイヤー名を更新します。
     */
    public void changePlayerName() {
        try {
            if (!exists())
                return;

            MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
            if (MySQLDBManager == null) {
                return;
            }
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement statement = conn.prepareStatement("UPDATE vote_monocraft SET player = ? WHERE uuid = ?")) {
                statement.setString(1, offplayer.getName());
                statement.setString(2, offplayer.getUniqueId().toString());// uuid
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    /**
     * プレイヤーのカスタムカラーを取得します。
     *
     * @return カスタムカラー
     */
    public String getCustomColor() {
        return customColor;
    }

    /**
     * プレイヤーのカスタムカラーを設定します。
     *
     * @param color カスタムカラー
     */
    public void setCustomColor(String color) {
        try {
            if (!exists())
                return;

            MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
            if (MySQLDBManager == null) {
                return;
            }
            Connection conn = MySQLDBManager.getConnection();
            try (PreparedStatement statement = conn.prepareStatement("UPDATE vote_monocraft SET color = ? WHERE uuid = ?")) {
                if (color == null) {
                    statement.setNull(1, Types.VARCHAR);
                } else {
                    statement.setString(1, color);
                }
                statement.setString(2, offplayer.getUniqueId().toString());// uuid
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }

    /**
     * データをフェッチします。forceがFalseの場合、最終情報取得から1時間経過していない場合キャッシュ情報を利用します。
     *
     * @param force 強制的にフェッチするか
     */
    public void fetchData(boolean force) {
        if (!force && ((dbSyncedTime + 60 * 60 * 1000) > System.currentTimeMillis())) {
            return; // 30分未経過
        }
        if (!MyMaidData.isMainDBActive()) {
            return;
        }
        try {
            Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();

            // このへんの処理綺麗に書きたい
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vote_monocraft WHERE uuid = ? ORDER BY id DESC LIMIT 1");
            stmt.setString(1, offplayer.getUniqueId().toString());

            try (ResultSet res = stmt.executeQuery()) {
                if (!res.next()) {
                    exists = false;
                    return;
                }

                this.id = res.getInt("id");
                this.count = res.getInt("count");
                this.lastVotedTime = res.getTimestamp("last").getTime() / 1000L;
                this.customColor = res.getString("color");
                this.dbSyncedTime = System.currentTimeMillis();
                exists = true;

                cache.put(offplayer.getUniqueId(), this);
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(getClass(), e);
        }
    }
}
