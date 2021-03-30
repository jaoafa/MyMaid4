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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.OfflinePlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.EBan;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

public class Cmd_EBan extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "eban",
            "EBanに関する処理を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットをEBanします。")
                .literal("add")
                .argument(OfflinePlayerArgument.of("player"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::addEBan)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットのEBanを解除します。")
                .literal("remove", "del", "rem")
                .argument(OfflinePlayerArgument.of("player"))
                .handler(this::removeEBan)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "EBan一覧を表示します。")
                .literal("status", "list")
                .argument(OfflinePlayerArgument
                    .<CommandSender>newBuilder("player")
                    .asOptional())
                .handler(this::getStatus)
                .build()
        );
    }

    void addEBan(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");
        String reason = context.get("reason");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        EBan eban = new EBan(player);
        EBan.Result result = eban.addBan(sender.getName(), reason);

        String message = "原因不明のエラーが発生しました。(成功しているかもしれません)";
        switch (result) {
            case ALREADY:
                message = "このプレイヤーは既にEBanに追加されています。";
                break;
            case DATABASE_NOT_ACTIVE:
                message = "データベースがアクティブではありません。";
                break;
            case DATABASE_ERROR:
                message = "データベースの操作中にエラーが発生しました。";
                break;
            case UNKNOWN_ERROR:
                message = "何らかのエラーが発生しました。";
                break;
        }

        SendMessage(sender, details(), String.format("プレイヤー「%s」のEBan追加に%sしました。",
            player.getName(),
            result == EBan.Result.SUCCESS ? "成功" : "失敗"));
        if (result != EBan.Result.SUCCESS) {
            SendMessage(sender, details(), String.format("理由: %s", message));
        }
    }

    void removeEBan(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        EBan eban = new EBan(player);
        EBan.Result result = eban.removeBan(sender.getName());

        String message = "原因不明のエラーが発生しました。(成功しているかもしれません)";
        switch (result) {
            case ALREADY:
                message = "このプレイヤーはEBanされていません。";
                break;
            case DATABASE_NOT_ACTIVE:
                message = "データベースがアクティブではありません。";
                break;
            case DATABASE_ERROR:
                message = "データベースの操作中にエラーが発生しました。";
                break;
            case UNKNOWN_ERROR:
                message = "何らかのエラーが発生しました。";
                break;
        }

        SendMessage(sender, details(), String.format("プレイヤー「%s」のEBan解除に%sしました。",
            player.getName(),
            result == EBan.Result.SUCCESS ? "成功" : "失敗"));
        if (result != EBan.Result.SUCCESS) {
            SendMessage(sender, details(), String.format("理由: %s", message));
        }
    }

    void getStatus(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.getOrDefault("player", null);

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        if (player != null) {
            sendPlayerStatus(sender, player);
        } else {
            sendEBanedList(sender);
        }
    }

    void sendEBanedList(CommandSender sender) {
        List<EBan.EBanData> ebans = EBan.getActiveEBans();
        if (ebans == null) {
            SendMessage(sender, details(), "EBan情報を取得できませんでした。時間をおいてもう一度お試しください。");
            return;
        }

        SendMessage(sender, details(), "現在、" + ebans.size() + "名のプレイヤーがEBanされています。");
        int nameWidth = ebans.stream()
            .filter(eban -> eban.getPlayerName() != null)
            .max(Comparator.comparingInt(i -> i.getPlayerName().length()))
            .filter(eban -> eban.getPlayerName() != null) // なんでこれが必要なのか…
            .map(eban -> eban.getPlayerName().length())
            .orElse(4);
        for (EBan.EBanData eban : ebans) {
            String displayName = "NULL";
            if (eban.getPlayerName() != null) {
                displayName = eban.getPlayerName();
            }
            SendMessage(sender, details(), formatText(displayName, nameWidth) + " " + eban.getReason());
        }
    }

    String formatText(String str, int width) {
        return width > str.length() ? str + StringUtils.repeat(" ", width - str.length()) : str;
    }

    void sendPlayerStatus(CommandSender sender, OfflinePlayer player) {
        EBan eban = new EBan(player);

        if (!eban.isBanned()) {
            SendMessage(sender, details(), "指定されたプレイヤーはEBanされていないようです。");
            return;
        }

        EBan.EBanData ebanData = eban.getEBanData();
        int ebanId = ebanData.getEBanId();
        String banned_by = ebanData.getBannedBy();
        String reason = ebanData.getReason();
        Timestamp created_at = ebanData.getCreatedAt();

        SendMessage(sender, details(), String.format("プレイヤー「%s」は現在EBanされています。", player.getName()));
        SendMessage(sender, details(), String.format("EBanId: %d", ebanId));
        SendMessage(sender, details(), String.format("EBan者: %s", banned_by));
        SendMessage(sender, details(), String.format("理由: %s", reason));
        SendMessage(sender, details(), "EBan日時: " + sdfFormat(created_at));
    }
}