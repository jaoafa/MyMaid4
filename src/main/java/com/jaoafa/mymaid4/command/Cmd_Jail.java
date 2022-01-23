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
import com.jaoafa.mymaid4.lib.Jail;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

public class Cmd_Jail extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "jail",
            "Jailに関する処理を行います。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットをJailします。")
                .literal("add")
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::addJail)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ターゲットのJailを解除します。")
                .literal("remove", "del", "rem")
                .argument(OfflinePlayerArgument.of("player"), ArgumentDescription.of("対象のプレイヤー"))
                .handler(this::removeJail)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "Jail一覧を表示します。")
                .literal("status", "list")
                .argument(OfflinePlayerArgument
                    .<CommandSender>newBuilder("player")
                    .asOptional(), ArgumentDescription.of("詳細を表示するプレイヤー"))
                .handler(this::getStatus)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "遺言を記録します。")
                .senderType(Player.class)
                .literal("testment")
                .argument(StringArgument.greedy("message"), ArgumentDescription.of("遺言"))
                .handler(this::setTestment)
                .build()
        );
    }

    void addJail(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");
        String reason = context.get("reason");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        Jail jail = Jail.getInstance(player);
        Jail.Result result = jail.addBan(sender.getName(), reason);

        String message = switch (result) {
            case ALREADY -> "このプレイヤーは既にJailに追加されています。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

        SendMessage(sender, details(), String.format("プレイヤー「%s」のJail追加に%sしました。",
            player.getName(),
            result == Jail.Result.SUCCESS ? "成功" : "失敗"));
        if (result != Jail.Result.SUCCESS) {
            SendMessage(sender, details(), String.format("理由: %s", message));
        }
    }

    void removeJail(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        OfflinePlayer player = context.get("player");

        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(sender, details(), "あなたはこのコマンドを実行できません。");
            return;
        }

        Jail jail = Jail.getInstance(player);
        Jail.Result result = jail.removeBan(sender.getName());

        String message = switch (result) {
            case ALREADY -> "このプレイヤーはJailされていません。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

        SendMessage(sender, details(), String.format("プレイヤー「%s」のJail解除に%sしました。",
            player.getName(),
            result == Jail.Result.SUCCESS ? "成功" : "失敗"));
        if (result != Jail.Result.SUCCESS) {
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
            sendJailedList(sender);
        }
    }

    void sendJailedList(CommandSender sender) {
        List<OfflinePlayer> jails = Jail.getBannedPlayers();
        if (jails == null) {
            SendMessage(sender, details(), "Jail情報を取得できませんでした。時間をおいてもう一度お試しください。");
            return;
        }

        SendMessage(sender, details(), "現在、" + jails.size() + "名のプレイヤーがJailされています。");
        int nameWidth = jails.stream()
            .filter(jail -> jail.getName() != null)
            .max(Comparator.comparingInt(i -> i.getName().length()))
            .filter(jail -> jail.getName() != null) // なんでこれが必要なのか…
            .map(jail -> jail.getName().length())
            .orElse(4);
        jails.forEach(p -> {
            Jail jail = Jail.getInstance(p);
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
        Jail jail = Jail.getInstance(player);

        if (!jail.isStatus()) {
            SendMessage(sender, details(), "指定されたプレイヤーはJailされていないようです。");
            return;
        }

        int jailId = jail.getJailId();
        String banned_by = jail.getBannedBy();
        String reason = jail.getReason();
        Timestamp created_at = jail.getCreatedAt();

        SendMessage(sender, details(), String.format("プレイヤー「%s」は現在Jailされています。", player.getName()));
        SendMessage(sender, details(), String.format("JailId: %d", jailId));
        SendMessage(sender, details(), String.format("Jail者: %s", banned_by));
        SendMessage(sender, details(), String.format("理由: %s", reason));
        SendMessage(sender, details(), "Jail日時: " + sdfFormat(created_at));
    }

    // ここに何らかの修正を加える場合、Cmd_Testmentにも修正を適用するのを忘れないでください。
    void setTestment(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String testment = context.get("message");

        Jail jail = Jail.getInstance(player);

        if (!jail.isStatus()) {
            SendMessage(player, details(), "あなたはJailされていないようです。");
            return;
        }

        Jail.Result result = jail.setTestment(testment);
        String message = switch (result) {
            case ALREADY -> "既にあなたは遺言を書いています。";
            case NOT_BANNED -> "あなたはJailされていないようです。";
            case DATABASE_NOT_ACTIVE -> "データベースがアクティブではありません。";
            case DATABASE_ERROR -> "データベースの操作中にエラーが発生しました。";
            case UNKNOWN_ERROR -> "何らかのエラーが発生しました。";
            default -> "原因不明のエラーが発生しました。(成功しているかもしれません)";
        };

        SendMessage(player, details(), String.format("遺言の記述に%sしました。",
            result == Jail.Result.SUCCESS ? "成功" : "失敗"));
        if (result != Jail.Result.SUCCESS) {
            SendMessage(player, details(), String.format("理由: %s", message));
        }
    }
}