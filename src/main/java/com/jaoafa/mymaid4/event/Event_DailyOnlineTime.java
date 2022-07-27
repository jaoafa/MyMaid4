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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Event_DailyOnlineTime extends MyMaidLibrary implements Listener, EventPremise {
    private final Map<UUID, Long> onlineTime = new HashMap<>();

    @Override
    public String description() {
        return "日毎のオンライン時間を計算しデータベースに登録します。";
    }

    /*
    ログイン時、プラグイン有効化時（=サーバ起動/リロード時）に記録を開始する。ログイン時間として現在時刻を追加
    ログアウト時、プラグイン無効化時（=サーバ停止/リロード時）に記録を終了する。ログアウト時間として現在時刻を追加し、DBに登録
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        onlineTime.put(uuid, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnable(PluginEnableEvent event) {
        for (Player player : event.getPlugin().getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            onlineTime.put(uuid, System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        recordDailyOnlineTime(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisable(PluginDisableEvent event) {
        for (Player player : event.getPlugin().getServer().getOnlinePlayers()) {
            recordDailyOnlineTime(player);
        }
    }

    void recordDailyOnlineTime(Player player) {
        MySQLDBManager manager = MyMaidData.getMainMySQLDBManager();
        if (manager == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();
        long loginTime = this.onlineTime.get(uuid);
        long diffSec = (time - loginTime) / 1000L;
        onlineTime.remove(uuid);

        Main.getMyMaidLogger().info("recordDailyOnlineTime: " + player.getName() + " " + diffSec);

        try {
            Connection conn = manager.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `daily-onlinetime` (uuid, onlinetime) VALUES (?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setLong(2, diffSec);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            MyMaidLibrary.reportError(this.getClass(), e);
        }
    }
}
