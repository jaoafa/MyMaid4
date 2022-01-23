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
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.*;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Cmd_DedMessage extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "dedmessage",
            "カスタム死亡メッセージを設定します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "今いる場所にカスタム死亡メッセージが設定されているかどうかを調べます。")
                .senderType(Player.class)
                .handler(this::getCustomDeathMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "カスタム死亡メッセージを登録します。")
                .senderType(Player.class)
                .literal("register")
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("カスタム死亡メッセージ（%player% はプレイヤー名、%killer% はキルした人の名前に置き換わります）"))
                .handler(this::addCustomDeathMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "死亡メッセージ表示を無効化します。")
                .senderType(Player.class)
                .literal("disable")
                .handler(this::addCustomDeathMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "カスタム死亡メッセージを削除します。")
                .senderType(Player.class)
                .literal("remove")
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("id")
                    .withSuggestionsProvider(this::suggestCustomDedMessageIds), ArgumentDescription.of("カスタム死亡メッセージID"))
                .handler(this::removeCustomDeathMessage)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "カスタム死亡メッセージの一覧を表示します。")
                .senderType(Player.class)
                .literal("list")
                .argument(IntegerArgument
                    .<CommandSender>newBuilder("page")
                    .withMin(1)
                    .asOptionalWithDefault("1"), ArgumentDescription.of("カスタム死亡メッセージID"))
                .handler(this::listCustomDeathMessage)
                .build()
        );
    }

    void getCustomDeathMessage(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Location loc = player.getLocation();

        if (!MyMaidData.isMainDBActive()) {
            SendMessage(player, details(), "メインデータベースに接続できないため、このコマンドは使用できません。");
            return;
        }

        DedMessage.Details details = DedMessage.match(loc.toVector());
        if (details == null) {
            SendMessage(player, details(), "あなたのいる場所にはカスタム死亡メッセージが登録されていません。");
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(details.getUUID());
        String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : details.getMinecraftID();
        SendMessage(player, details(), "あなたのいる場所にはカスタム死亡メッセージが登録されています。");
        SendMessage(player, details(), MessageFormat.format("死亡メッセージ: {0}", details.getMessage()));
        SendMessage(player, details(), MessageFormat.format("対象エリア: {0} - {1}", formatLocation(details.getFirstLocation()), formatLocation(details.getSecondLocation())));
        SendMessage(player, details(), MessageFormat.format("登録者: {0}", name));
        SendMessage(player, details(), MessageFormat.format("作成日: {0}", sdfFormat(details.getCreatedAt())));
    }

    void addCustomDeathMessage(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        WorldEditPlugin we = Main.getWorldEdit();

        if (we == null) {
            SendMessage(player, details(), "WorldEditプラグインが動作していないため、このコマンドを使用できません。");
            return;
        }

        String deathMessage = context.getOrDefault("message", null);
        try {
            World selectionWorld = we.getSession(player).getSelectionWorld();
            if (selectionWorld == null) {
                SendMessage(player, details(), "WorldEditで範囲を指定してください。");
                return;
            }
            Region region = we.getSession(player).getSelection(selectionWorld);
            BlockVector3 blockMin = region.getMinimumPoint();
            Vector vectorMin = new Vector(blockMin.getBlockX(), blockMin.getBlockY(), blockMin.getBlockZ());
            BlockVector3 blockMax = region.getMaximumPoint();
            Vector vectorMax = new Vector(blockMax.getBlockX(), blockMax.getBlockY(), blockMax.getBlockZ());

            DedMessage.Details details = new DedMessage.Details(player, deathMessage, Bukkit.getWorld(selectionWorld.getName()), vectorMin, vectorMax);
            boolean bool = DedMessage.add(player, details);
            String pre = deathMessage != null ? "カスタム死亡メッセージの登録" : "死亡メッセージ表示の無効化登録";
            SendMessage(player, details(), pre + "に" + (bool ? "成功" : "失敗") + "しました。");
        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
        }
    }

    void removeCustomDeathMessage(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int id = context.get("id");
        DedMessage.Details details = DedMessage.get(id);
        if (details == null) {
            SendMessage(player, details(), "指定されたIDのカスタム死亡メッセージは見つかりませんでした。");
            return;
        }
        if (details.getUUID() != player.getUniqueId() && !isAM(player)) {
            SendMessage(player, details(), "指定されたIDのカスタム死亡メッセージはあなたが作成したものではないようです。");
            return;
        }
        boolean bool = DedMessage.remove(player, id);
        SendMessage(player, details(), "カスタム死亡メッセージの削除に" + (bool ? "成功" : "失敗") + "しました。");
    }

    void listCustomDeathMessage(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int page = context.<Integer>get("page");

        Set<DedMessage.Details> details = DedMessage.getAll();

        int pageItemNum = 5;
        int skipNum = (page - 1) * pageItemNum;

        Component header = Component.text().append(
            Component.text("----- "),
            Component.text("DEDMESSAGE MARKERS", NamedTextColor.GOLD),
            Component.text(" -----")
        ).build();
        SendMessage(player, details(), header);

        details.stream().skip(skipNum).limit(pageItemNum).forEach(m -> {
            Component componentHomeInfo = Component.text().append(
                Component.text(formatText(String.valueOf(m.getId()), 3), NamedTextColor.GOLD),
                Component.text(formatText(m.getWorld().getName(), 10), NamedTextColor.AQUA)
                    .hoverEvent(HoverEvent.showText(Component.text(m.getWorld().getName()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getFirstLocation().getX()), 6))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getFirstLocation().getX()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getFirstLocation().getY()), 5))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getFirstLocation().getY()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getFirstLocation().getZ()), 6))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getFirstLocation().getZ()))),
                Component.space(),
                Component.text("～"),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getSecondLocation().getX()), 6))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getSecondLocation().getX()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getSecondLocation().getY()), 5))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getSecondLocation().getY()))),
                Component.space(),
                Component.text(formatText(String.format("%.1f", m.getSecondLocation().getZ()), 6))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getSecondLocation().getZ()))),
                Component.space(),
                Component.text(formatText(m.getMessage(), 10))
                    .hoverEvent(HoverEvent.showText(Component.text(m.getMessage())))
            ).build();
            SendMessage(player, details(), componentHomeInfo);
        });
        Component footer = Component.text().append(
            Component.text("<[PREV] ")
                .clickEvent(ClickEvent.runCommand(String.format("/dedmessage list %d", page - 1))),
            Component.text("[" + (page - 1) + "] PAGE", NamedTextColor.GOLD),
            Component.text(" [NEXT]>")
                .clickEvent(ClickEvent.runCommand(String.format("/dedmessage list %d", page + 1)))
        ).build();
        SendMessage(player, details(), footer);
    }

    String formatLocation(Vector loc) {
        return MessageFormat.format("{0} {1} {2}", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    String formatText(String str, int width) {
        return width == str.length() ? str :
            width > str.length() ? str + StringUtils.repeat(" ", width - str.length()) :
                str.substring(0, width);
    }

    List<String> suggestCustomDedMessageIds(CommandContext<CommandSender> context, String current) {
        Player player = (Player) context.getSender();
        return DedMessage.getAll().stream()
            .filter(d -> d.getUUID() == player.getUniqueId())
            .map(DedMessage.Details::getId)
            .map(String::valueOf)
            .collect(Collectors.toList());
    }
}
