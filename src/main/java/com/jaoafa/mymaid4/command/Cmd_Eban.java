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
import com.jaoafa.mymaid4.lib.Eban;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

public class Cmd_Eban extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "eban",
            "Ebanに関する処理を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットをEbanします。")
                .literal("add")
                .argument(OfflinePlayerArgument.of("player"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::addEban)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットのEbanを解除します。")
                .literal("remove", "del", "rem")
                .argument(OfflinePlayerArgument.of("player"))
                .handler(this::removeEban)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "Eban一覧を表示します。")
                .literal("status", "list")
                .argument(OfflinePlayerArgument
                    .<CommandSender>newBuilder("player")
                    .asOptional())
                .handler(this::getStatus)
                .build()
        );
    }

    void addEban(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");
        String reason = context.get("reason");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        Eban eban = new Eban(player);
        Eban.Result result = eban.addBan(sender.getName(), reason);

        String message = "原因不明のエラーが発生しました。(成功しているかもしれません)";
        switch (result) {
            case ALREADY:
                message = "このプレイヤーは既にEbanに追加されています。";
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

        SendMessage(sender, details(), String.format("プレイヤー「%s」のEban追加に%sしました。",
            player.getName(),
            result == Eban.Result.SUCCESS ? "成功" : "失敗"));
        if (result != Eban.Result.SUCCESS) {
            SendMessage(sender, details(), String.format("理由: %s", message));
        }
    }

    void removeEban(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        Eban eban = new Eban(player);
        Eban.Result result = eban.removeBan(sender.getName());

        String message = "原因不明のエラーが発生しました。(成功しているかもしれません)";
        switch (result) {
            case ALREADY:
                message = "このプレイヤーはEbanされていません。";
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

        SendMessage(sender, details(), String.format("プレイヤー「%s」のEban解除に%sしました。",
            player.getName(),
            result == Eban.Result.SUCCESS ? "成功" : "失敗"));
        if (result != Eban.Result.SUCCESS) {
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
            sendEbanedList(sender);
        }
    }

    void sendEbanedList(CommandSender sender) {
        List<Eban.EbanData> ebans = Eban.getActiveEbans();
        if (ebans == null) {
            SendMessage(sender, details(), "Eban情報を取得できませんでした。時間をおいてもう一度お試しください。");
            return;
        }

        SendMessage(sender, details(), "現在、" + ebans.size() + "名のプレイヤーがEbanされています。");
        int nameWidth = ebans.stream()
            .filter(eban -> eban.getPlayerName() != null)
            .max(Comparator.comparingInt(i -> i.getPlayerName().length()))
            .filter(eban -> eban.getPlayerName() != null) // なんでこれが必要なのか…
            .map(eban -> eban.getPlayerName().length())
            .orElse(4);
        for (Eban.EbanData eban : ebans) {
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
        Eban eban = new Eban(player);

        if (!eban.isBanned()) {
            SendMessage(sender, details(), "指定されたプレイヤーはEbanされていないようです。");
            return;
        }

        Eban.EbanData ebanData = eban.getEbanData();
        int ebanId = ebanData.getEbanId();
        String banned_by = ebanData.getBannedBy();
        String reason = ebanData.getReason();
        Timestamp created_at = ebanData.getCreatedAt();

        SendMessage(sender, details(), String.format("プレイヤー「%s」は現在Ebanされています。", player.getName()));
        SendMessage(sender, details(), String.format("EbanId: %d", ebanId));
        SendMessage(sender, details(), String.format("Eban者: %s", banned_by));
        SendMessage(sender, details(), String.format("理由: %s", reason));
        SendMessage(sender, details(), "Eban日時: " + sdfFormat(created_at));
    }
}