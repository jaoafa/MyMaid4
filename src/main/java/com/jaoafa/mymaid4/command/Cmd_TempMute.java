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
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_TempMute extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "tempmute",
            "一時的なミュートを実施します。運営のみ使用できます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "TempMuteをオン・オフします。指定しない場合、トグルで変更します。")
                .senderType(Player.class)
                .argument(BooleanArgument
                    .<CommandSender>newBuilder("changeTo")
                    .asOptional(), ArgumentDescription.of("オン・オフのいずれか (未指定の場合トグル)"))
                .handler(this::changeTempMute)
                .build()
        );
    }

    void changeTempMute(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Boolean bool = context.getOrDefault("changeTo", null);

        if (bool == null) {
            bool = !MyMaidData.getTempMuting().contains(player);
        }

        if (bool) {
            MyMaidData.addTempMuting(player);
        } else {
            MyMaidData.removeTempMuting(player);
        }
        SendMessage(player, details(), "一時的なミュートを " + (bool ? "オン" : "オフ") + " にしました。");
        if (bool)
            SendMessage(player, details(), Component.text("ミュートしている間、自分を含む全てのチャットおよび一部のシステムメッセージは送信されなくなります。", NamedTextColor.RED));
    }
}
