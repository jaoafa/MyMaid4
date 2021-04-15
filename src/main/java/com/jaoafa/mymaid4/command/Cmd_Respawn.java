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
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Cmd_Respawn extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "respawn",
            "プレイヤーをリスポーンします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーをリスポーンします。")
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("リスポーンさせるプレイヤー"))
                .handler(this::respawnPlayer)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定した秒数後にプレイヤーをリスポーンします。")
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("リスポーンさせるプレイヤー"))
                .argument(LongArgument.of("delay"), ArgumentDescription.of("遅延させる秒数"))
                .handler(this::respawnPlayerDelay)
                .build()
        );
    }

    void respawnPlayer(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            SendMessage(sender, details(), "プレイヤーが指定されていません。");
            return;
        }

        player.spigot().respawn();
        SendMessage(sender, details(), player.getName() + "をリスポーンしました。");
    }

    void respawnPlayerDelay(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            SendMessage(sender, details(), "プレイヤーが指定されていません。");
            return;
        }
        long delay = context.get("delay");
        if (delay <= 0) {
            SendMessage(sender, details(), "遅延させる秒数を指定してください。");
            return;
        }

        SendMessage(sender, details(), delay + "秒後に" + player.getName() + "をリスポーンさせます。");
        new BukkitRunnable() {
            public void run() {
                player.spigot().respawn();
            }
        }.runTaskLater(Main.getJavaPlugin(), delay * 20L);
    }
}
