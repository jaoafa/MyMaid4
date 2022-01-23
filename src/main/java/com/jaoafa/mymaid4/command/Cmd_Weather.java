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
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Cmd_Weather extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "weather",
            "自分だけに適用される天気を設定します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "自分だけに適用される天気を設定します。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("weatherName")
                    .withSuggestionsProvider(this::suggestWeatherName), ArgumentDescription.of("天気名"))
                .senderType(Player.class)
                .handler(this::weatherSetByName)
                .build()
        );
    }

    void weatherSetByName(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String weatherName = context.get("weatherName");
        if (weatherName.equalsIgnoreCase("rain")) weatherName = "downfall";

        boolean isValidWeatherName = false;
        for (WeatherType weather : WeatherType.values()) {
            if (!isValidWeatherName) isValidWeatherName = weather.name().equalsIgnoreCase(weatherName);
        }

        if (!isValidWeatherName) {
            SendMessage(player, details(), "そのような天気はありません！");
            return;
        }

        WeatherType weatherType = WeatherType.valueOf(weatherName.toUpperCase());
        //設定
        player.setPlayerWeather(weatherType);
        //お知らせ
        SendMessage(player, details(), String.format("あなたの天気を%sに設定しました！", weatherType.name()));
    }

    List<String> suggestWeatherName(final CommandContext<CommandSender> context, final String current) {
        List<String> list = Arrays.stream(WeatherType.values())
            .map(Enum::name).toList();

        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(current.toLowerCase()))
            .collect(Collectors.toList());
    }
}
