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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.Home;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Cmd_DelHome extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "delhome",
            Arrays.asList("removehome", "remhome"),
            "ホームを削除します。"
        );
    }


    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "デフォルトホームを削除します。")
                .senderType(Player.class)
                .handler(this::deleteHome)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定された名前のホームを削除します。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("name")
                    .asOptionalWithDefault("default")
                    .withSuggestionsProvider(Home::suggestHomeName), ArgumentDescription.of("ホーム名"))
                .handler(this::deleteHome)
                .build()
        );
    }

    void deleteHome(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String name = context.getOrDefault("name", "default");
        Home home = new Home(player);

        if (!home.exists(name)) {
            SendMessage(player, details(), String.format("指定されたホーム「%s」は見つかりません。", name));
            SendMessage(player, details(), Component.text().append(
                Component.text("ホームの作成は", NamedTextColor.GREEN),
                Component.space(),
                Component.text(String.format("/sethome %s", name), NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(
                        Component.text("コマンド「" + String.format("/sethome %s", name) + "」をサジェストします。")
                    ))
                    .clickEvent(ClickEvent.suggestCommand(String.format("/sethome %s", name))),
                Component.space(),
                Component.text("を実行してください。", NamedTextColor.GREEN)
            ).build());
            return;
        }

        Home.Detail detail = home.get(name);
        if (Bukkit.getWorld(detail.worldName()) == null) {
            SendMessage(player, details(), String.format("ホーム「%s」のワールド「%s」が見つかりませんでした。", name, detail.worldName()));
            return;
        }
        home.remove(name);

        SendMessage(player, details(), String.format("ホーム「%s (%s %.2f %.2f %.2f %.2f %.2f)」を削除しました。",
            name,
            detail.worldName(),
            detail.x(),
            detail.y(),
            detail.z(),
            detail.yaw(),
            detail.pitch()));
    }
}
