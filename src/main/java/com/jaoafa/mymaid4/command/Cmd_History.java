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
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.Historyjao;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cmd_History extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "history",
            "jaoHistoryに関する操作を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのjaoHistoryにデータを追加します。")
                .literal("add")
                .argument(OfflinePlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("メッセージ"))
                .handler(this::addItem)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのjaoHistory項目を無効化します。")
                .literal("disable")
                .argument(OfflinePlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(IntegerArgument.<CommandSender>newBuilder("item")
                    .withSuggestionsProvider(this::suggestHistoryIds), ArgumentDescription.of("jaoHistoryId"))
                .handler(this::disableItem)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのjaoHistory項目の通知設定を行います。")
                .literal("notify")
                .argument(OfflinePlayerArgument.of("target"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(IntegerArgument.<CommandSender>newBuilder("item")
                    .withSuggestionsProvider(this::suggestHistoryIds), ArgumentDescription.of("jaoHistoryId"))
                .argument(BooleanArgument
                    .<CommandSender>newBuilder("changeTo")
                    .withLiberal(true)
                    .withSuggestionsProvider(this::suggestBoolean), ArgumentDescription.of("変更後の通知設定"))
                .handler(this::notifyItem)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーのjaoHistory情報を表示します。")
                .literal("status")
                .argument(OfflinePlayerArgument.of("target"))
                .handler(this::viewStatus)
                .build()
        );
    }

    void addItem(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        if (sender instanceof BlockCommandSender) {
            SendMessage(sender, details(), "コマンドブロックからではこのコマンドを実行することができません！");
            return;
        }
        OfflinePlayer target = context.get("target");
        String message = context.get("message");

        Historyjao histjao = Historyjao.getHistoryjao(target);
        boolean bool = histjao.add(message, context.getSender());
        SendMessage(sender, details(), String.format("%s の jaoHistory への登録に%sしました。", target.getName(), bool ? "成功" : "失敗"));
    }

    void disableItem(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (sender instanceof Player && !isAM((Player) sender)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        if (sender instanceof BlockCommandSender) {
            SendMessage(sender, details(), "コマンドブロックからではこのコマンドを実行することができません！");
            return;
        }
        OfflinePlayer target = context.get("target");
        int id = context.get("item");

        Historyjao histjao = Historyjao.getHistoryjao(target);
        boolean bool = histjao.disable(id);
        SendMessage(sender, details(), String.format("%s の jaoHistory のアイテム %d の無効化に%sしました。", target.getName(), id, bool ? "成功" : "失敗"));
    }

    void notifyItem(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SendMessage(sender, details(), "changeTo: " + context.get("changeTo"));
        if (sender instanceof Player && !isAM((Player) sender)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        if (sender instanceof BlockCommandSender) {
            SendMessage(sender, details(), "コマンドブロックからではこのコマンドを実行することができません！");
            return;
        }
        OfflinePlayer target = context.get("target");
        int id = context.get("item");
        boolean changeTo = context.get("changeTo");

        Historyjao histjao = Historyjao.getHistoryjao(target);
        boolean bool = histjao.setNotify(id, changeTo);
        SendMessage(sender, details(), String.format("%s の jaoHistory のアイテム %d の通知設定を%sにするのに%sしました。", target.getName(), id, changeTo ? "オン" : "オフ", bool ? "成功" : "失敗"));
    }

    void viewStatus(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたの権限ではこのコマンドを実行することができません！");
            return;
        }
        if (sender instanceof BlockCommandSender) {
            SendMessage(sender, details(), "コマンドブロックからではこのコマンドを実行することができません！");
            return;
        }
        OfflinePlayer target = context.get("target");

        Historyjao histjao = Historyjao.getHistoryjao(target);
        if (!histjao.isFound()) {
            SendMessage(sender, details(), "指定されたプレイヤーのデータは見つかりませんでした。");
            return;
        }
        SendMessage(sender, details(), String.format("----- %s -----", target.getName()));
        List<Historyjao.Data> list = histjao.getDataList();
        for (Historyjao.Data data : list) {
            SendMessage(sender, details(), "[" + data.id + "] " + data.message + " - " + sdfFormat(data.getCreatedAt()));
        }
    }

    List<String> suggestHistoryIds(final CommandContext<CommandSender> context, final String current) {
        OfflinePlayer player = context.getOrDefault("player", null);
        if (player == null) return new ArrayList<>();
        return Historyjao.getHistoryjao(player).getDataList().stream()
            .map(item -> String.valueOf(item.id))
            .filter(s -> s.startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }

    List<String> suggestBoolean(final CommandContext<CommandSender> context, final String current) {
        return Stream.of("TRUE", "YES", "ON", "FALSE", "NO", "OFF")
            .filter(s -> s.startsWith(current.toUpperCase()))
            .collect(Collectors.toList());
    }
}
