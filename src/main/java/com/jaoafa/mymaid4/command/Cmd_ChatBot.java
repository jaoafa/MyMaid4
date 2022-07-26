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
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MeboChatBot;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Cmd_ChatBot extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "chatbot",
            List.of("q", "question", "質問", "しつもん"),
            "よくある質問への応対をします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "よくある質問への応対をします。")
                .argument(StringArgument.greedy("text"))
                .handler(this::question)
                .build()
        );
    }

    void question(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String text = context.get("text");

        if (Main.getMeboChatBot() == null) {
            SendMessage(player, details(), "チャットに必要な情報が定義されていません。");
            return;
        }

        MeboChatBot chatBot = Main.getMeboChatBot();
        MeboChatBot.MeboResponse response = chatBot.chat(player, text);
        if (response == null) {
            SendMessage(player, details(), "チャットに必要な情報が定義されていません。");
            return;
        }
        if (!response.status()) {
            SendMessage(player, details(), "なんかうまくいきませんでした。");
            return;
        }
        SendMessage(player, details(), response.message());
    }
}
