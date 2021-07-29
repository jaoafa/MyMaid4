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
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_SecretTP extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "secrettp",
            "スぺテクターでテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "スぺテクターで特定のプレイヤーにテレポートします。")
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("テレポートするプレイヤー名"))
                .handler(this::secretTP)
                .build()
        );
    }

    void secretTP(CommandContext<CommandSender> context) {
        Player target = context.getOrDefault("player", null);
        Player player = (Player) context.getSender();
        if (target == null) return;
        if (!isAMR(player)) {
            SendMessage(target, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        target.setGameMode(GameMode.SPECTATOR);
        target.teleport(target);
        SendMessage(player,details(),target.getName()+"にスぺテクターでテレポートしました。");
    }
}