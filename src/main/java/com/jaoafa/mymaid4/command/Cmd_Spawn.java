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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Cmd_Spawn extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "spawn",
            List.of("hub", "lobby"),
            "スポーン地点にテレポートします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "ワールドのスポーン地点にテレポートします。")
                .senderType(Player.class)
                .handler(this::teleportWorldSpawn)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "あなたのスポーン地点にテレポートします。")
                .senderType(Player.class)
                .literal("true")
                .handler(this::teleportYourSpawn)
                .build()
        );
    }

    void teleportWorldSpawn(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        World world = player.getWorld();
        Location loc = world.getSpawnLocation();
        loc = loc.add(0.5f, 0f, 0.5f);
        player.teleport(loc);
        SendMessage(player, details(), "スポーン地点にテレポートしました。");
    }

    void teleportYourSpawn(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        Location loc = player.getBedSpawnLocation();
        if (loc == null) {
            SendMessage(player, details(), "スポーン地点が見つかりませんでした。/spawnpointで設定しましょう。");
            return;
        }
        player.teleport(loc);
        SendMessage(player, details(), "あなたのスポーン地点にテレポートしました。");
    }
}
