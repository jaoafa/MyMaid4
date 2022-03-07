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
import cloud.commandframework.arguments.standard.IntegerArgument;
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
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Home extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "home",
            "ホームにテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "デフォルトホームにテレポートします。")
                .senderType(Player.class)
                .handler(this::teleportHome)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定された名前のホームにテレポートします。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("name")
                    .asOptionalWithDefault("default")
                    .withSuggestionsProvider(Home::suggestHomeName), ArgumentDescription.of("ページ"))
                .handler(this::teleportHome)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ホーム一覧を表示します。")
                .senderType(Player.class)
                .literal("list")
                .handler(this::listHome)
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("Page").withMin(1)
                    .asOptionalWithDefault("1"), ArgumentDescription.of("ページ"))
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したホームに関する情報を表示します。")
                .senderType(Player.class)
                .literal("view")
                .argument(StringArgument
                    .<CommandSender>newBuilder("name")
                    .asOptionalWithDefault("default")
                    .withSuggestionsProvider(Home::suggestHomeName), ArgumentDescription.of("ページ"))
                .handler(this::viewHome)
                .build()
        );
    }

    void teleportHome(CommandContext<CommandSender> context) {
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
        player.teleport(detail.getLocation());

        SendMessage(player, details(), String.format("ホーム「%s」にテレポートしました。", name));
    }

    void listHome(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int pagenumInt = context.get("Page");
        if (pagenumInt == 0) {
            SendMessage(player, details(), "ページ数は1以上の数字を指定してください。");
            return;
        }
        int visualPagenum;
        try {
            visualPagenum = pagenumInt;
        } catch (NumberFormatException e) {
            SendMessage(player, details(), "ページ数は半角数字で指定してください。");
            return;
        }
        int listPagenum = visualPagenum - 1;

        int listBeginnum = listPagenum * 5;

        Component componentHeader = Component.text().append(
            Component.text("===// "),
            Component.text("HOMELIST", NamedTextColor.GOLD),
            Component.text(" //===")
        ).build();
        SendMessage(player, details(), componentHeader);

        Home home = new Home(player);
        int finalVisualPagenum = visualPagenum;
        int finalVisualPagenumBefore = visualPagenum - 1;
        int finalVisualPagenumAfter = visualPagenum + 1;
        home.getHomes().stream().skip(listBeginnum).limit(5).forEach(s -> {
            String homename = cutHomeName(s.name());
            Component componentHomeInfo = Component.text().append(
                Component.text("["),
                Component.text(homename, Style.style().color(NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand("/home " + s.name())).build()),
                Component.text("] "),
                Component.text(" ("),
                Component.text(s.worldName(), NamedTextColor.AQUA),
                Component.text(" x:" + String.valueOf(s.x()).split("\\.")[0] + " y:" + String.valueOf(s.y()).split("\\.")[0] + " z:" + String.valueOf(s.z()).split("\\.")[0] + ")")
            ).build();
            SendMessage(player, details(), componentHomeInfo);
        });
        Component componentHomeInfo = Component.text().append(
            Component.text("<[PREV] ", Style.style().clickEvent(ClickEvent.runCommand("/home list " + finalVisualPagenumBefore)).build()),
            Component.text("[" + finalVisualPagenum + "] PAGE", NamedTextColor.GOLD),
            Component.text(" [NEXT]>", Style.style().clickEvent(ClickEvent.runCommand("/home list " + finalVisualPagenumAfter)).build())
        ).build();
        SendMessage(player, details(), componentHomeInfo);
    }


    void viewHome(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String name = context.getOrDefault("name", "default");
        Home home = new Home(player);
        if (!home.exists(name)) {
            SendMessage(player, details(), "指定されたホームが見つかりませんでした。");
            return;
        }

        Home.Detail detail = home.get(name);

        SendMessage(player, details(), String.format("----- %s -----", detail.name()));
        SendMessage(player, details(), String.format(
            "Location: %s %.2f %.2f %.2f %.2f %.2f",
            detail.worldName(),
            detail.x(),
            detail.y(),
            detail.z(),
            detail.yaw(),
            detail.pitch()
        ));
        SendMessage(player, details(), "作成日時: " + detail.getDate());
    }

    String cutHomeName(String homename) {
        int homenameLength = homename.length();
        if (homenameLength >= 8) {
            homename = homename.substring(0, 5) + "...";
        } else {
            homename += " ".repeat(8 - homenameLength);
        }
        return homename;
    }
}
