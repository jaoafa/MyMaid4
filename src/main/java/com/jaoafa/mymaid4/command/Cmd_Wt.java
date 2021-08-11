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
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Cmd_Wt extends MyMaidLibrary implements CommandPremise {

    final Map<String, String> worlds = new HashMap<>() {
        {
            put("1", "Jao_Afa");
            put("2", "Jao_Afa_nether");
            put("3", "SandBox");
        }
    };

    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "wt",
            "他ワールドにテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(cloud.commandframework.Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したワールドにテレポートします。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("worldName")
                    .withSuggestionsProvider(MyMaidLibrary::suggestWorldNames), ArgumentDescription.of("ワールド名もしくはワールド番号"))
                .handler(this::worldTeleport)
                .build(),

            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーを指定したワールドにテレポートさせます。")
                .argument(StringArgument
                    .<CommandSender>newBuilder("worldName")
                    .withSuggestionsProvider(MyMaidLibrary::suggestWorldNames), ArgumentDescription.of("ワールド名もしくはワールド番号"))
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("テレポートさせるプレイヤー"))
                .handler(this::worldTeleportPlayer)
                .build()

        );
    }

    void worldTeleport(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        String worldName = context.get("worldName");
        World world = Bukkit.getWorld(worlds.getOrDefault(worldName, worldName));
        if (world == null) {
            SendMessage(player, details(), String.format("「%s」ワールドの取得に失敗しました。", worlds.getOrDefault(worldName, worldName)));
            return;
        }
        Location loc = new Location(world, 0, 0, 0, 0, 0);
        int y = getGroundPos(loc);
        loc = new Location(world, 0, y, 0, 0, 0);
        loc.add(0.5f, 0f, 0.5f);
        player.teleport(loc);
        SendMessage(player, details(), String.format("「%s」ワールドにテレポートしました。", world.getName()));
    }


    void worldTeleportPlayer(CommandContext<CommandSender> context) {
        Player player = context.getOrDefault("player", null);
        if (player == null) {
            return;
        }
        CommandSender sender = context.getSender();
        if (sender instanceof Player && !isAMR((Player) sender)) {
            SendMessage(player, details(), "あなたは他人をテレポートさせることはできません");
            return;
        }
        String worldName = context.get("worldName");
        World world = Bukkit.getWorld(worlds.getOrDefault(worldName, worldName));
        if (world == null) {
            SendMessage(player, details(), String.format("「%s」ワールドの取得に失敗しました。", worlds.getOrDefault(worldName, worldName)));
            return;
        }
        Location loc = new Location(world, 0, 0, 0, 0, 0);
        int y = getGroundPos(loc);
        loc = new Location(world, 0, y, 0, 0, 0);
        loc.add(0.5f, 0f, 0.5f);
        player.teleport(loc);
        SendMessage(player, details(), String.format("「%s」ワールドにテレポートしました。", world.getName()));
        SendMessage(sender, details(), String.format("プレイヤー「%s」を「%s」ワールドにテレポートさせました。", player, world.getName()));
    }
}
