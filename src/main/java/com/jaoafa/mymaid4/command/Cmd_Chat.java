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
        colors.add("color:blue");
        colors.add("color:red");
        colors.add("color:green");
        colors.add("color:white");
        colors.add("color:dark_blue");
        colors.add("color:dark_purple");
        colors.add("color:light_purple");
        colors.add("color:aqua");
        colors.add("color:dark_aqua");
        colors.add("color:dark_gray");
        colors.add("color:dark_green");
        colors.add("color:dark_red");
        colors.add("color:black");
        colors.add("color:gold");
        colors.add("color:yellow");
        return colors;
    }


    void chatFake(CommandContext<CommandSender> context) {

        final String[] msg = {context.getOrDefault("text", "")};

        //color:をchatcolorとして認識
        final ChatColor[] chatColor = new ChatColor[1];
        Arrays.stream(msg[0].split(" ")).forEach(s -> {
            if (s.startsWith("color:")) {
                if (s.equals("color:blue")) {
                    chatColor[0] = ChatColor.BLUE;
                }
                if (s.equals("color:red")) {
                    chatColor[0] = ChatColor.RED;
                }
                if (s.equals("color:green")) {
                    chatColor[0] = ChatColor.GREEN;
                }
                if (s.equals("color:white")) {
                    chatColor[0] = ChatColor.WHITE;
                }
                if (s.equals("color:dark_blue")) {
                    chatColor[0] = ChatColor.DARK_BLUE;
                }
                if (s.equals("color:dark_purple")) {
                    chatColor[0] = ChatColor.DARK_PURPLE;
                }
                if (s.equals("color:light_purple")) {
                    chatColor[0] = ChatColor.LIGHT_PURPLE;
                }
                if (s.equals("color:aqua")) {
                    chatColor[0] = ChatColor.AQUA;
                }
                if (s.equals("color:dark_aqua")) {
                    chatColor[0] = ChatColor.DARK_AQUA;
                }
                if (s.equals("color:dark_gray")) {
                    chatColor[0] = ChatColor.DARK_GRAY;
                }
                if (s.equals("color:dark_green")) {
                    chatColor[0] = ChatColor.DARK_GREEN;
                }
                if (s.equals("color:dark_red")) {
                    chatColor[0] = ChatColor.DARK_RED;
                }
                if (s.equals("color:black")) {
                    chatColor[0] = ChatColor.BLACK;
                }
                if (s.equals("color:gold")) {
                    chatColor[0] = ChatColor.GOLD;
                }
                if (s.equals("color:yellow")) {
                    chatColor[0] = ChatColor.YELLOW;
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


        /*context.getOrDefault(0)
        ChatColor color = ChatColor.GRAY;
        List<String> colors = Arrays.stream(args).filter(
            arg -> arg != null && arg.startsWith("color:")).collect(Collectors.toList());
        if (colors.size() != 0) {
            for (ChatColor cc : ChatColor.values()) {
                if (!cc.name().equalsIgnoreCase(colors.get(0).substring("color:".length()))) {
                    continue;
                }
                color = cc;
            }
        }
        List<String> texts = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).filter(
            arg -> arg != null && !arg.startsWith("color:")).collect(Collectors.toList());
        String text = ChatColor.translateAlternateColorCodes('&', String.join(" ", texts));
        if (args[0].equalsIgnoreCase("jaotan")) {
            color = ChatColor.GOLD;
        }
        chatFake(color, args[0], text);*/
    }
}
