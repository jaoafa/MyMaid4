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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Cmd_GetUserKey extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "getuserkey",
            "ユーザーを認証するためのキー(ユーザーキー)に関する操作を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ユーザーキーを生成し、表示します。")
                .senderType(Player.class)
                .handler(this::createUserKey)
                .build()
        );
    }

    void createUserKey(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            public void run() {
                if (!MyMaidData.isMainDBActive()) {
                    SendMessage(player, details(), "データベースがアクティブではないため、ユーザーキーを作成できませんでした。");
                    return;
                }

                try {
                    Connection conn = MyMaidData.getMainMySQLDBManager().getConnection();
                    String userKey = getUserKeyExistCheck(conn, uuid);

                    // 既に発行しているものがない場合
                    if (userKey == null) {
                        userKey = getUserKey(conn);

                        PreparedStatement statement = conn.prepareStatement(
                            "INSERT INTO userkey (player, uuid, userkey) VALUES (?, ?, ?);");
                        statement.setString(1, player.getName());
                        statement.setString(2, uuid.toString());
                        statement.setString(3, userKey);
                        statement.executeUpdate();
                        statement.close();
                    }

                    // 発行できなかった場合
                    if (userKey == null) {
                        SendMessage(player, details(), "UserKeyを生成できませんでした。時間をおいて再度お試しください。");
                        return;
                    }

                    Component component = Component.text().append(
                        Component.text("あなたのUserKeyは「", NamedTextColor.GREEN),
                        Component.space(),
                        Component.text(userKey, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(Component.text("クリックするとユーザーキーをコピーします。")))
                            .clickEvent(ClickEvent.copyToClipboard(userKey)),
                        Component.space(),
                        Component.text("」です。", NamedTextColor.GREEN)
                    ).build();
                    SendMessage(player, details(), component);
                    SendMessage(player, details(), "ユーザーキーをクリックすると、クリップボードにコピーします。");
                } catch (SQLException e) {
                    SendMessage(player, details(), "データベースの操作に失敗しました。時間をおいてからもう一度お試しください。");
                    reportError(getClass(), e);
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    String getUserKey(Connection conn) throws SQLException {
        String userKey;
        while (true) {
            userKey = RandomStringUtils.randomAlphabetic(10);
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM userkey WHERE userkey = ?")) {
                statement.setString(1, userKey);
                ResultSet res = statement.executeQuery();
                if (!res.next()) {
                    statement.close();
                    return userKey;
                }
            }
        }
    }

    String getUserKeyExistCheck(Connection conn, UUID uuid) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM userkey WHERE uuid = ? AND used = ?")) {
            statement.setString(1, uuid.toString());
            statement.setBoolean(2, false);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                String userKey = res.getString("userkey");
                statement.close();
                return userKey;
            }
        }
        return null;
    }
}
