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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                .argument(StringArgument.newBuilder("text"))
                .senderType(Player.class)
                .handler(this::chatFake)
                .build()
        );
    }

    void chatFake(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        SendMessage(player, details(), context.getRawInput().get(0));
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
