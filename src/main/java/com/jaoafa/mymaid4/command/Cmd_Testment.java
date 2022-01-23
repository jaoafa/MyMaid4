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

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.Jail;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_Testment extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "testment",
            "Jailの遺言を残します。jail testmentのエイリアスです。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "遺言を記録します。")
                .senderType(Player.class)
                .argument(StringArgument.greedy("message"))
                .handler(this::setTestment) // 本来ならproxiesを使うべきだが、MyMaid4のコマンド登録仕様により使用不能
                .build()
        );
    }

    // ここに何らかの修正を加える場合、Cmd_Jailにも修正を適用するのを忘れないでください。
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
