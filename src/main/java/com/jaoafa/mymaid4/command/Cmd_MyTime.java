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
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class Cmd_MyTime extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "mytime",
            "自分だけに適用される時間を設定します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される時間を設定します。")
                .literal("set")
                .argument(IntegerArgument.of("timeInt"))
                .argument(BooleanArgument.of("isRelative"))
                .senderType(Player.class)
                .handler(this::myTimeSetByInt)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される時間を設定します。")
                .literal("set")
                .argument(StringArgument.of("timeName"))
                .argument(BooleanArgument.of("isRelative"))
                .senderType(Player.class)
                .handler(this::myTimeSetByName)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される時間を進めます。")
                .literal("add")
                .argument(IntegerArgument.of("timeInt"))
                .argument(BooleanArgument.of("isRelative"))
                .senderType(Player.class)
                .handler(this::myTimeAddByInt)
                .build()
        );
    }

    void myTimeSetByInt(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int myTimeInt = context.get("timeInt");
        boolean isRelative = context.get("isRelative");
        player.setPlayerTime(myTimeInt,isRelative);
        SendMessage(player,details(), String.format("あなたの時間を%sに設定しました！", myTimeInt));
    }
    void myTimeSetByName(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String myTimeName = context.get("timeName");
        boolean isRelative = context.get("isRelative");

        Map<String, Integer> timeNameToInt = new HashMap<>();
        timeNameToInt.put("day",1000);
        timeNameToInt.put("noon",6000);
        timeNameToInt.put("night",13000);
        timeNameToInt.put("midnight",18000);

        if (!timeNameToInt.containsKey(myTimeName)){
            SendMessage(player,details(),"そのような名前の時間は存在しません！");
            return;
        }
        player.setPlayerTime(timeNameToInt.get(myTimeName),isRelative);
        SendMessage(player,details(), String.format("あなたの時間を%sに設定しました！", timeNameToInt.get(myTimeName)));
    }
    void myTimeAddByInt(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int myTimeInt = context.get("timeInt");
        boolean isRelative = context.get("isRelative");
        long myCurrentTime = player.getPlayerTime();
        player.setPlayerTime(myCurrentTime+myTimeInt,isRelative);
        SendMessage(player,details(), String.format("あなたの時間を%s進めました！", myTimeInt));
    }
}
