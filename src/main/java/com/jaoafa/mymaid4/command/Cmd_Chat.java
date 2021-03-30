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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cmd_Chat extends MyMaidLibrary implements CommandPremise {

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "chat",
            "偽のプレイヤーに喋らせます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {

        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "偽のプレイヤーに喋らせます。")
                .argument(StringArgument.<CommandSender>newBuilder("text")
                    .greedy().withSuggestionsProvider(this::colors))
                .senderType(Player.class)
                .handler(this::chatFake)
                .build()
        );
    }

    private List<String> colors(@NonNull CommandContext<CommandSender> commandSenderCommandContext, @NonNull String s) {
        ArrayList<String> colors = new ArrayList<>();
        for (ChatColor color : ChatColor.values()) {
            colors.add("color:" + color.name().toLowerCase());
        }
        return colors;
    }


    void chatFake(CommandContext<CommandSender> context) {

        final String[] msg = {context.getOrDefault("text", "")};

        //color:をchatcolorとして認識
        final ChatColor[] chatColor = new ChatColor[1];
        Arrays.stream(msg[0].split(" ")).forEach(s -> {
            if (s.startsWith("color:")) {
                for (ChatColor color : ChatColor.values()) {
                    if (s.equals("color:" + color.name().toLowerCase())) {
                        chatColor[0] = color;
                    }
                }
                msg[0] = msg[0].replaceFirst(s, "");
                msg[0] = msg[0].trim();
            }
        });

        //colorを抜いた最初の引数をfakePlayerをして認識
        String fakePlayer = msg[0].split(" ")[0];

        msg[0] = msg[0].replaceFirst(fakePlayer, "");
        msg[0] = msg[0].trim();

        //color null check
        if (chatColor[0] == null) {
            chatColor[0] = ChatColor.GRAY;
        }
        //player jaotan check
        if (fakePlayer.equals("jaotan")) {
            chatColor[0] = ChatColor.GOLD;
        }

        chatFake(chatColor[0], fakePlayer, msg[0]);
    }
}
