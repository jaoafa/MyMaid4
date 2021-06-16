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

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;


public class Cmd_Time extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "time",
            "自分だけに適用される時間を設定します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される時間を設定します。")
                .literal("set")
                .argument(StringArgument.of("timeName"), ArgumentDescription.of("時間の名前"))
                .argument(BooleanArgument.optional("isRelative"), ArgumentDescription.of("ワールド時間と相対的に保つか"))
                .senderType(Player.class)
                .handler(this::timeSetByName)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される時間を進めます。")
                .literal("add")
                .argument(IntegerArgument.of("timeInt"), ArgumentDescription.of("時間"))
                .argument(BooleanArgument.optional("isRelative"), ArgumentDescription.of("ワールド時間と相対的に保つか"))
                .senderType(Player.class)
                .handler(this::timeAddByInt)
                .build()
        );
    }

    void timeSetByName(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String timeName = context.get("timeName"); //day or 1000 どれが入ってるかわからない
        @SuppressWarnings("ConstantConditions")
        boolean isRelative = context.getOrDefault("isRelative", false); //時間固定するかどうか
        int timeInt = 0; //1000とかで指定された場合に代入
        boolean getTimeByNumber; //数値指定かどうか

        try {
            //とりあえずパースしてみる。成功したら数値指定をtrueに
            timeInt = Integer.parseInt(timeName);
            getTimeByNumber = true;
        } catch (NumberFormatException e) {
            //出来なかったら文字指定なので数値指定はfalse
            getTimeByNumber = false;
        }

        //文字指定->数値への変換Map
        Map<String, Integer> timeNameToInt = new HashMap<>();
        timeNameToInt.put("day", 1000);
        timeNameToInt.put("noon", 6000);
        timeNameToInt.put("night", 13000);
        timeNameToInt.put("midnight", 18000);

        if (!getTimeByNumber && !timeNameToInt.containsKey(timeName)) {
            //変換Mapに存在しなかった場合
            SendMessage(player, details(), "そのような名前の時間は存在しません！");
            return;
        }

        //最終的に設定する時間
        int resultTime = getTimeByNumber
            ? timeInt
            : timeNameToInt.get(timeName);

        //設定
        player.setPlayerTime(resultTime, isRelative);
        //お知らせ
        SendMessage(player, details(), String.format("あなたの時間を%sに設定しました！", resultTime));
    }

    void timeAddByInt(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        int myTimeInt = context.get("timeInt");
        @SuppressWarnings("ConstantConditions")
        boolean isRelative = context.getOrDefault("isRelative", false); //時間固定するかどうか
        long myCurrentTime = player.getPlayerTime();
        player.setPlayerTime(myCurrentTime + myTimeInt, isRelative);
        SendMessage(player, details(), String.format("あなたの時間を%s進めました！", myTimeInt));
    }
}
