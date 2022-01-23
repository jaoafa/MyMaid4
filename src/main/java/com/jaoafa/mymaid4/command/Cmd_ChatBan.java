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
import com.jaoafa.mymaid4.lib.ChatBan;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

public class Cmd_ChatBan extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "chatban",
            "ChatBanに関する処理を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットをChatBanします。")
                .literal("add")
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(StringArgument.greedy("reason"), ArgumentDescription.of("理由"))
                .handler(this::addChatBan)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットのChatBanを解除します。")
                .literal("remove", "del", "rem")
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::removeChatBan)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ChatBan一覧もしくは詳細を表示します。")
                .literal("status", "list")
                .argument(OfflinePlayerArgument
                    .<CommandSender>newBuilder("player")
                    .asOptional(), ArgumentDescription.of("詳細を表示するプレイヤー"))
                .handler(this::getStatus)
                .build()
        );
    }

    void addChatBan(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");
        String reason = context.get("reason");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        ChatBan chatBan = ChatBan.getInstance(player);
        ChatBan.Result result = chatBan.addBan(sender.getName(), reason);

        String message = switch (result) {
            case ALREADY -> "このプレイヤーは既にChatBanに追加されています。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

        SendMessage(sender, details(), String.format("プレイヤー「%s」のChatBan追加に%sしました。",
            player.getName(),
            result == ChatBan.Result.SUCCESS ? "成功" : "失敗"));
        if (result != ChatBan.Result.SUCCESS) {
            SendMessage(sender, details(), String.format("理由: %s", message));
        }
    }

    void removeChatBan(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        ChatBan chatBan = ChatBan.getInstance(player);
        ChatBan.Result result = chatBan.removeBan(sender.getName());

        String message = switch (result) {
            case ALREADY -> "このプレイヤーはChatBanされていません。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

        SendMessage(sender, details(), String.format("プレイヤー「%s」のChatBan解除に%sしました。",
            player.getName(),
            result == ChatBan.Result.SUCCESS ? "成功" : "失敗"));
        if (result != ChatBan.Result.SUCCESS) {
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
            sendChatBanedList(sender);
        }
    }

    void sendChatBanedList(CommandSender sender) {
        List<OfflinePlayer> chatbans = ChatBan.getBannedPlayers();
        if (chatbans == null) {
            SendMessage(sender, details(), "ChatBan情報を取得できませんでした。時間をおいてもう一度お試しください。");
            return;
        }

        SendMessage(sender, details(), "現在、" + chatbans.size() + "名のプレイヤーがChatBanされています。");
        int nameWidth = chatbans.stream()
            .filter(jail -> jail.getName() != null)
            .max(Comparator.comparingInt(i -> i.getName().length()))
            .filter(jail -> jail.getName() != null) // なんでこれが必要なのか…
            .map(jail -> jail.getName().length())
            .orElse(4);
        chatbans.forEach(p -> {
            ChatBan jail = ChatBan.getInstance(p);
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
        ChatBan chatBan = ChatBan.getInstance(player);

        if (!chatBan.isStatus()) {
            SendMessage(sender, details(), "指定されたプレイヤーはChatBanされていないようです。");
            return;
        }

        int chatbanId = chatBan.getChatBanId();
        String banned_by = chatBan.getBannedBy();
        String reason = chatBan.getReason();
        Timestamp created_at = chatBan.getCreatedAt();

        SendMessage(sender, details(), String.format("プレイヤー「%s」は現在ChatBanされています。", player.getName()));
        SendMessage(sender, details(), String.format("ChatBanId: %d", chatbanId));
        SendMessage(sender, details(), String.format("ChatBan者: %s", banned_by));
        SendMessage(sender, details(), String.format("理由: %s", reason));
        SendMessage(sender, details(), "ChatBan日時: " + sdfFormat(created_at));
    }
}