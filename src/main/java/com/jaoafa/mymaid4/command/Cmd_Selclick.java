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

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SelClickManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Selclick extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "selclick",
            "SelClickの有効/無効を切り替えます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "SelClickの有効/無効を切り替えます。")
                .senderType(Player.class)
                .handler(this::changeStatus)
                .build()
        );
    }

    void changeStatus(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (SelClickManager.isEnable(player)) {
            SelClickManager.setStatus(player, false);
            SendMessage(player, details(), "SelClickを無効化しました。");
        } else {
            SelClickManager.setStatus(player, true);
            SendMessage(player, details(), "SelClickを有効化しました。");
            SendMessage(player, details(), "走りながら空気を左クリックするとWorldEditの選択を解除することができます。");
        }
    }
}