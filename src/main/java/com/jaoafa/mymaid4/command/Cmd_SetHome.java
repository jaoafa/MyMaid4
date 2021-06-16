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
import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.Home;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class Cmd_SetHome extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "sethome",
            Collections.singletonList("addhome"),
            "ホームを設定します。"
        );
    }


    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "デフォルトホームを設定します。")
                .senderType(Player.class)
                .handler(this::setHome)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定された名前のホームを設定します。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("name")
                    .asOptionalWithDefault("default"), ArgumentDescription.of("ホーム名。指定しない場合default"))
                .handler(this::setHome)
                .build()
        );
    }

    void setHome(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String name = context.getOrDefault("name", "default");
        Home home = new Home(player);

        if (home.exists(name)) {
            SendMessage(player, details(), String.format("指定されたホーム「%s」は既に存在します。", name));
            SendMessage(player, details(), Component.text().append(
                Component.text("ホームの削除は", NamedTextColor.GREEN),
                Component.space(),
                Component.text(String.format("/delhome %s", name), NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(
                        Component.text("コマンド「" + String.format("/delhome %s", name) + "」をサジェストします。")
                    ))
                    .clickEvent(ClickEvent.suggestCommand(String.format("/delhome %s", name))),
                Component.space(),
                Component.text("を実行してください。", NamedTextColor.GREEN)
            ).build());
            return;
        }

        Location loc = player.getLocation();
        home.set(name, loc);
        Achievementjao.getAchievementAsync(player, Achievement.HEREISHOUSE); // No.26 ここがおうち

        SendMessage(player, details(), String.format("ホーム「%s (%s %.2f %.2f %.2f %.2f %.2f)」を追加しました。",
            name,
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch()));
    }
}
