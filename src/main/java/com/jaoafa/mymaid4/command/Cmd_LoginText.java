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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMCJP;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_LoginText extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "logintext",
            "ログイン時に表示されるテキストに任意のテキストを追加します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ログイン時に表示されるテキストをリセットします。")
                .senderType(Player.class)
                .handler(this::setLoginText)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "ログイン時に表示されるテキストに任意のテキストを追加します。")
                .senderType(Player.class)
                .argument(StringArgument.greedy("loginText"), ArgumentDescription.of("変更後のログインテキスト"))
                .handler(this::setLoginText)
                .build()
        );
    }

    void setLoginText(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String loginText = context.getOrDefault("loginText", null);
        PlayerVoteDataMCJP pvd = new PlayerVoteDataMCJP(player);

        int count = pvd.getVoteCount();

        if (count < 200) {
            // 200未満の場合カスタムメッセージ変更不可
            SendMessage(player, details(), "あなたの投票数ではカスタムログインテキストの登録ができません。minecraft.jpで200投票を目指しましょう！");
            return;
        }

        if (loginText != null && !PlayerVoteDataMCJP.checkCustomLoginText(loginText)) {
            // 200未満の場合カスタムメッセージ変更不可
            SendMessage(player, details(), "指定されたカスタムログインテキストは正しくありません。全角/半角スペースを含めることはできず、30文字以内でなければなりません。");
            return;
        }

        pvd.setCustomLoginText(loginText);
        SendMessage(player, details(), "カスタムログインテキストを" + (loginText != null ? "設定" : "リセット") + "しました！");
    }
}
