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
import cloud.commandframework.arguments.standard.FloatArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd_WalkSpeed extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "walkspeed",
            "移動速度を変更します。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "指定したプレイヤーの移動速度を表示します。")
                .argument(PlayerArgument.optional("target"),
                    ArgumentDescription.of("ターゲットプレイヤー"))
                .handler(this::showWalkSpeed)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "移動速度を設定します。")
                .senderType(Player.class)
                .literal("set")
                .argument(FloatArgument.of("percent"),
                    ArgumentDescription.of("移動速度(通常100%)"))
                .handler(this::setWalkSpeed)
                .build()
        );
    }

    void showWalkSpeed(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        if (!context.contains("target") && !(sender instanceof Player)) {
            SendMessage(sender, details(), "ターゲットプレイヤーが指定されていません。");
            return;
        }
        Player target = context.getOrDefault("target", (Player) sender);
        if (target == null) {
            SendMessage(sender, details(), "ターゲットプレイヤーが指定されていません。");
            return;
        }
        float speed = target.getWalkSpeed() * 1000;
        SendMessage(sender, details(), Component.text().append(
            Component.text(sender == target ? "あなた" : target.getName(), NamedTextColor.GREEN),
            Component.text("の移動速度は", NamedTextColor.GREEN),
            Component.space(),
            Component.text(speed, NamedTextColor.GREEN),
            Component.text("%", NamedTextColor.GREEN),
            Component.space(),
            Component.text("です。", NamedTextColor.GREEN)
        ).build());
    }

    void setWalkSpeed(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        float speed = context.<Float>get("percent") / 1000;
        if (speed < -1 || speed > 1) {
            SendMessage(player, details(), "値は 1000% から -1000% を指定できます。");
            return;
        }
        player.setWalkSpeed(speed);
        SendMessage(player, details(), Component.text().append(
            Component.text("あなたの移動速度を", NamedTextColor.GREEN),
            Component.space(),
            Component.text(speed * 1000, NamedTextColor.GREEN),
            Component.text("%", NamedTextColor.GREEN),
            Component.space(),
            Component.text("に変更しました。", NamedTextColor.GREEN)
        ).build());
    }
}
