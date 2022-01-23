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
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(StringArgument.greedy("reason"), ArgumentDescription.of("理由"))
                .handler(this::addEBan)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットのEBanを解除します。")
                .literal("remove", "del", "rem")
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
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

        EBan eban = EBan.getInstance(player);
        EBan.Result result = eban.addBan(sender.getName(), reason);

        String message = switch (result) {
            case ALREADY -> "このプレイヤーは既にEBanに追加されています。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

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

        EBan eban = EBan.getInstance(player);
        EBan.Result result = eban.removeBan(sender.getName());

        String message = switch (result) {
            case ALREADY -> "このプレイヤーはEBanされていません。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

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
        List<OfflinePlayer> ebans = EBan.getBannedPlayers();
        if (ebans == null) {
            SendMessage(sender, details(), "EBan情報を取得できませんでした。時間をおいてもう一度お試しください。");
            return;
        }

        SendMessage(sender, details(), "現在、" + ebans.size() + "名のプレイヤーがEBanされています。");
        int nameWidth = ebans.stream()
            .filter(jail -> jail.getName() != null)
            .max(Comparator.comparingInt(i -> i.getName().length()))
            .filter(jail -> jail.getName() != null) // なんでこれが必要なのか…
            .map(jail -> jail.getName().length())
            .orElse(4);
        ebans.forEach(p -> {
            EBan jail = EBan.getInstance(p);
            String displayName = "NULL";
            if (p.getName() != null) {
                displayName = p.getName();
            }
            SendMessage(sender, details(), formatText(displayName, nameWidth) + " " + jail.getReason());
        });
    }

    String formatText(String str, int width) {
        return width > str.length() ? str + StringUtils.repeat(" ", width - str.length()) : str;
    }

    void sendPlayerStatus(CommandSender sender, OfflinePlayer player) {
        EBan eban = EBan.getInstance(player);

        if (!eban.isStatus()) {
            SendMessage(sender, details(), "指定されたプレイヤーはEBanされていないようです。");
            return;
        }

        int ebanId = eban.getEBanId();
        String banned_by = eban.getBannedBy();
        String reason = eban.getReason();
        Timestamp created_at = eban.getCreatedAt();

        SendMessage(sender, details(), String.format("プレイヤー「%s」は現在EBanされています。", player.getName()));
        SendMessage(sender, details(), String.format("EBanId: %d", ebanId));
        SendMessage(sender, details(), String.format("EBan者: %s", banned_by));
        SendMessage(sender, details(), String.format("理由: %s", reason));
        SendMessage(sender, details(), "EBan日時: " + sdfFormat(created_at));
    }
}