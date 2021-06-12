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
import cloud.commandframework.bukkit.arguments.selector.SingleEntitySelector;
import cloud.commandframework.bukkit.parsers.selector.SingleEntitySelectorArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Optional;

public class Cmd_Rider extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "rider",
            "プレイヤーやエンティティに乗ったり下ろしたりします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤー・エンティティに乗ります。")
                .senderType(Player.class)
                .argument(SingleEntitySelectorArgument.optional("target"),
                    ArgumentDescription.of("対象のプレイヤー・エンティティ。指定しない場合見ているプレイヤー"))
                .handler(this::ridEntity)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "プレイヤー・エンティティをプレイヤー・エンティティに乗せます。")
                .literal("ride")
                .argument(SingleEntitySelectorArgument.of("from"),
                    ArgumentDescription.of("乗せるプレイヤー・エンティティ"))
                .argument(SingleEntitySelectorArgument.of("to"),
                    ArgumentDescription.of("乗せられるプレイヤー・エンティティ"))
                .handler(this::ridEntityToEntity)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "乗っているプレイヤー・エンティティを下ろします。")
                .literal("leave")
                .argument(SingleEntitySelectorArgument.optional("target"),
                    ArgumentDescription.of("対象のプレイヤー・エンティティ。指定しない場合実行者"))
                .handler(this::leaveEntity)
                .build()
        );
    }

    void ridEntity(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
        Entity target = null;
        if (targetSelector != null && targetSelector.hasAny()) {
            target = targetSelector.getEntity();
        }

        if (target == null) {
            Optional<Player> looking = player.getWorld().getPlayers().stream().filter(p -> getLookingAt(player, p)).findFirst();
            if (looking.isEmpty()) {
                SendMessage(player, details(), "あなたはどのプレイヤーも見ていません...");
                return;
            }
            target = looking.get();
        }
        if (player.getUniqueId() == target.getUniqueId()) {
            SendMessage(player, details(), "あなたはあなたです。");
            return;
        }
        boolean bool = target.addPassenger(player);
        SendMessage(player, details(), String.format("ターゲット「%s (%s)」に%s。",
            target.getName(),
            target.getType().name(),
            bool ? "乗りました" : "乗れませんでした"));
    }

    void ridEntityToEntity(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SingleEntitySelector fromSelector = context.get("from");
        Entity from = null;
        if (fromSelector.hasAny()) {
            from = fromSelector.getEntity();
        }
        SingleEntitySelector toSelector = context.get("to");
        Entity to = null;
        if (toSelector.hasAny()) {
            to = toSelector.getEntity();
        }
        if (from == null) {
            SendMessage(sender, details(), "乗るプレイヤー・エンティティが指定されていません");
            return;
        }
        if (to == null) {
            SendMessage(sender, details(), "乗られるプレイヤー・エンティティが指定されていません");
            return;
        }
        if (from.getUniqueId() == to.getUniqueId()) {
            SendMessage(sender, details(), "乗るプレイヤー・エンティティと乗られるプレイヤー・エンティティが同じです。");
            return;
        }
        boolean bool = to.addPassenger(from);
        SendMessage(sender, details(), String.format("「%s (%s)」は「%s (%s)」に%s。",
            from.getName(),
            from.getType().name(),
            to.getName(),
            to.getType().name(),
            bool ? "乗りました" : "乗れませんでした"));
    }

    void leaveEntity(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
        Entity target = null;
        if (targetSelector != null && targetSelector.hasAny()) {
            target = targetSelector.getEntity();
        }
        if (target == null) {
            if (!(context.getSender() instanceof Player)) {
                SendMessage(context.getSender(), details(), "targetが必要です。");
                return;
            }
            target = (Player) context.getSender();
        }
        for (Entity entity : target.getPassengers()) {
            if (target.removePassenger(entity)) {
                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろしました。", entity.getName(), entity.getType().name()));
            } else if (entity.getVehicle() != null && entity.getVehicle().eject()) {
                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろしました。", entity.getName(), entity.getType().name()));
            } else {
                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろせませんでした。", entity.getName(), entity.getType().name()));
            }
        }
    }
}