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
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class Event_LoginSuccessful extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログインに成功したことをデータベースに書き込みます。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            public void run() {
                MySQLDBManager MySQLDBManager = MyMaidData.getMainMySQLDBManager();
                if (MySQLDBManager == null) {
                    return;
                }
                try {
                    Connection conn = MySQLDBManager.getConnection();
                    try (PreparedStatement statement = conn.prepareStatement(
                        "UPDATE login SET login_success = ? WHERE uuid = ? ORDER BY id DESC LIMIT 1")) {
                        statement.setBoolean(1, true);
                        statement.setString(2, uuid.toString());
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    MyMaidLibrary.reportError(getClass(), e);
                }

            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}
