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
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class Cmd_Wtp extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "wtp",
            "他ワールドの特定座標にテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(cloud.commandframework.Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したワールドの特定座標にテレポートします。")
                .senderType(Player.class)
                .argument(StringArgument
                    .<CommandSender>newBuilder("worldName")
                    .withSuggestionsProvider(MyMaidLibrary::suggestWorldNames), ArgumentDescription.of("ワールド名もしくはワールド番号"))
                .argument(DoubleArgument.of("x"), ArgumentDescription.of("X座標"))
                .argument(DoubleArgument.of("y"), ArgumentDescription.of("Y座標"))
                .argument(DoubleArgument.of("z"), ArgumentDescription.of("Z座標"))
                .argument(FloatArgument.optional("yaw", 0), ArgumentDescription.of("Yaw"))
                .argument(FloatArgument.optional("pitch", 0), ArgumentDescription.of("Pitch"))
                .argument(PlayerArgument.optional("player"), ArgumentDescription.of("テレポートさせるプレイヤー"))
                .handler(this::worldTeleport)
                .build()
        );
    }

    void worldTeleport(CommandContext<CommandSender> context) {
        if (context.getOptional("player").isPresent()) {
            worldTeleportPlayer(context);
            return;
        }
        Player player = (Player) context.getSender();
        String worldName = context.get("worldName");
        double x = context.get("x");
        double y = context.get("y");
        double z = context.get("z");
        float yaw = context.get("yaw");
        float pitch = context.get("pitch");
        Map<String, String> worlds = MyMaidData.getAliasWorlds();
        World world = Bukkit.getWorld(worlds.getOrDefault(worldName, worldName));
        if (world == null) {
            SendMessage(player, details(), String.format("「%s」ワールドの取得に失敗しました。", worlds.getOrDefault(worldName, worldName)));
            return;
        }
        Location loc = new Location(world, x, y, z, yaw, pitch);
        if (x % 1 == 0 && y % 1 == 0 && z % 1 == 0) {
            loc.add(0.5f, 0f, 0.5f);
        }
        player.teleport(loc);
        SendMessage(player, details(), "「%s」ワールドの%f %f %fにテレポートしました。".formatted(worldName, x, y, z));
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
        double x = context.get("x");
        double y = context.get("y");
        double z = context.get("z");
        float yaw = context.get("yaw");
        float pitch = context.get("pitch");
        Map<String, String> worlds = MyMaidData.getAliasWorlds();
        World world = Bukkit.getWorld(worlds.getOrDefault(worldName, worldName));
        if (world == null) {
            SendMessage(player, details(), String.format("「%s」ワールドの取得に失敗しました。", worlds.getOrDefault(worldName, worldName)));
            return;
        }
        Location loc = new Location(world, x, y, z, yaw, pitch);
        if (x % 1 == 0 && y % 1 == 0 && z % 1 == 0) {
            loc.add(0.5f, 0f, 0.5f);
        }
        player.teleport(loc);
        SendMessage(player, details(), "「%s」ワールドの%f %f %fにテレポートしました。".formatted(worldName, x, y, z));
        SendMessage(sender, details(), "プレイヤー「%s」を「%s」ワールドの%f %f %fにテレポートしました。".formatted(player, worldName, x, y, z));
    }
}
