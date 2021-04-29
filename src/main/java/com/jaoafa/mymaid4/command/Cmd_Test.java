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

package com.jaoafa.mymaid4.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class Cmd_Test extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "test",
            "MyMaidテストコマンド"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .literal("version", ArgumentDescription.of("バージョンを表示します。"))
                .handler(context -> {
                    final CommandSender sender = context.getSender();
                    SendMessage(sender, details(), "Version: " + Main.getJavaPlugin().getDescription().getVersion());
                })
                .build(),
            builder
                .literal("database", ArgumentDescription.of("データベースへの接続を試行します。"))
                .handler(context -> {
                    final CommandSender sender = context.getSender();
                    try {
                        MyMaidData.getMainMySQLDBManager().getConnection();
                        SendMessage(sender, details(), "Main: 接続成功");
                    } catch (SQLException e) {
                        SendMessage(sender, details(), "Main: 接続失敗");
                    }
                    try {
                        MyMaidData.getZKRHatMySQLDBManager().getConnection();
                        SendMessage(sender, details(), "ZakuroHat: 接続成功");
                    } catch (SQLException e) {
                        SendMessage(sender, details(), "ZakuroHat: 接続失敗");
                    }
                })
                .build(),
            builder
                .literal("nbt", ArgumentDescription.of("NBTタグを取得します。"))
                .handler(context -> {
                    final CommandSender sender = context.getSender();
                    Player player = (Player) sender;
                    SendMessage(sender, details(),
                        NMSManager.getNBT(
                            player.getInventory().getItemInMainHand()
                        )
                    );
                })
                .build()
        );
    }
}
