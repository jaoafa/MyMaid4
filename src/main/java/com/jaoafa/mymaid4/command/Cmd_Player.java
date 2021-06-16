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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Cmd_Player extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "player",
            Collections.singletonList("group"),
            "プレイヤーの権限グループを取得します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "あなたの権限グループを表示します。")
                .senderType(Player.class)
                .handler(this::getPermGroup)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定されたプレイヤーの権限グループを表示します。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("player")
                    .withSuggestionsProvider(this::suggestOfflinePlayers), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::getPermGroup)
                .build()
        );
    }

    @SuppressWarnings("deprecation")
    void getPermGroup(CommandContext<CommandSender> context) {
        OfflinePlayer player;
        if (context.contains("player")) {
            player = Bukkit.getOfflinePlayer(context.<String>get("player"));
        } else {
            player = (Player) context.getSender();
        }

        if (player.getName() == null) {
            SendMessage(context.getSender(), details(), "プレイヤー情報が取得できませんでした。");
            return;
        }

        String mainGroup = getPermissionMainGroup(player);
        if (mainGroup == null) {
            SendMessage(context.getSender(), details(), "権限グループ情報が取得できませんでした。");
            return;
        }

        Component component = Component.text().append(
            Component.text(player.getName() + "さんの権限グループ:").style(Style.style(NamedTextColor.GREEN)),
            Component.space(),
            Component.text(mainGroup).style(Style.style(NamedTextColor.GREEN))
        ).build();
        SendMessage(context.getSender(), details(), component);
    }
}
