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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_InvEdit extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "invedit",
            "プレイヤーのインベントリを編集します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .senderType(Player.class)
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのインベントリを編集します。")
                .argument(PlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::editInventory)
                .build()
        );
    }

    void editInventory(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        Player player = (Player) sender;
        Player target = context.get("target");
        if (!isAM(player)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを使用できません。");
            return;
        }

        player.openInventory(target.getInventory());
    }
}
