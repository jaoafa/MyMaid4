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
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public class Cmd_Looking extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "looking",
            Arrays.asList("eye", "see", "look"),
            "プレイヤーを見続けます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "今見ているプレイヤーを見続けはじめます。")
                .senderType(Player.class)
                .handler(this::startNowLooking)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤーを見続けはじめます。")
                .senderType(Player.class)
                .literal("on", "see", "start")
                .argument(PlayerArgument.of("target"), ArgumentDescription.of("見続けるプレイヤー"))
                .handler(this::startLooking)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤーを見続けるのをやめます。")
                .senderType(Player.class)
                .literal("off", "stop", "end")
                .handler(this::endLooking)
                .build()
        );
    }

    void startNowLooking(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Optional<Player> target = player.getWorld().getPlayers().stream().filter(p -> getLookingAt(player, p)).findFirst();
        if (target.isEmpty()) {
            SendMessage(player, details(), "あなたはどのプレイヤーも見ていません...");
            return;
        }
        MyMaidData.setLooking(player.getUniqueId(), target.get().getUniqueId());
        SendMessage(player, details(), target.get().getName() + " を見続けます…");
    }

    void startLooking(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Player target = context.getOrDefault("target", null);
        if (target == null) {
            SendMessage(player, details(), "見続けるターゲットを指定してください");
            return;
        }
        MyMaidData.setLooking(player.getUniqueId(), target.getUniqueId());
        SendMessage(player, details(), target.getName() + " を見続けます…");
    }

    void endLooking(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        if (!MyMaidData.isLooking(player.getUniqueId())) {
            SendMessage(player, details(), "あなたは誰も見ていません");
            return;
        }
        MyMaidData.removeLooking(player.getUniqueId());
        SendMessage(player, details(), "見続けるのをやめました");
    }
}
