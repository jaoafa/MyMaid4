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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Cmd_Lead extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "Lead",
            "プレイヤー・エンティティにリードを付けます。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(
            builder
                .meta(CommandMeta.DESCRIPTION, "1辺30ブロックの立方体内にある指定された名前のエンティティにリードを付けます。a")
                .literal("set")
                .argument(SingleEntitySelectorArgument.optional("target"),
                    ArgumentDescription.of("対象のプレイヤー・エンティティ。指定しない場合実行者"))
                .handler(this::leadEntity)
//                .build(),
//            builder
//                .meta(CommandMeta.DESCRIPTION, "[プレイヤーもしくはエンティティ(Rider)]から[プレイヤーもしくはエンティティ(Riding)]にリードを付けます。")
//                .literal("set")
//                .argument(SingleEntitySelectorArgument.of("from"),
//                    ArgumentDescription.of("リードを持っているプレイヤー・エンティティ"))
//                .argument(SingleEntitySelectorArgument.of("to"),
//                    ArgumentDescription.of("リードを付けられるプレイヤー・エンティティ"))
//                .handler(this::leadEntityToEntity)
//                .build(),
//            builder
//                .meta(CommandMeta.DESCRIPTION, "[プレイヤー・エンティティ]に付いているリードを外します。")
//                .literal("leave")
//                .argument(SingleEntitySelectorArgument.optional("target"),
//                    ArgumentDescription.of("リードが付いているプレイヤー・エンティティ。指定しない場合実行者"))
//                .handler(this::leaveEntity)
//                .build()
//            builder   // lead set <X1> <Y1> <Z1> to <X2> <Y2> <Z2>
//                .meta(CommandMeta.DESCRIPTION, "[座標1にあるフェンス]から[座標2(に出現するコウモリ)]にリードを付けます。")
//                .literal("set")
//                .argument(SingleEntitySelectorArgument.of("posX1"),
//                    ArgumentDescription.of("フェンスのx座標"))
//                .argument(SingleEntitySelectorArgument.of("posY1"),
//                    ArgumentDescription.of("フェンスのy座標"))
//                .argument(SingleEntitySelectorArgument.of("posZ1"),
//                    ArgumentDescription.of("フェンスのz座標"))
//                .literal("to")
//                .argument(SingleEntitySelectorArgument.of("posX2"),
//                    ArgumentDescription.of("出現させるコウモリのx座標"))
//                .argument(SingleEntitySelectorArgument.of("posY2"),
//                    ArgumentDescription.of("出現させるコウモリのy座標"))
//                .argument(SingleEntitySelectorArgument.of("posZ2"),
//                    ArgumentDescription.of("出現させるコウモリのz座標"))
//                .handler(this::leadFenceToBat)
//                .build(),
//            builder   // lead set <x2> <x2> <x2>
//                .meta(CommandMeta.DESCRIPTION, "[1辺30ブロックの立方体内の最も近くにいるコウモリ]から[座標2(に出現するコウモリ)]にリードを付けます。")
//                .literal("set")
//                .argument(SingleEntitySelectorArgument.optional("target"),
//                    ArgumentDescription.of("対象のプレイヤー・エンティティ。指定しない場合実行者"))
//                .handler(this::leaveEntity)
                .build()
        );
    }

    void leadEntity(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
        LivingEntity target = null;

        if (targetSelector != null && targetSelector.hasAny() && targetSelector.getEntity() instanceof LivingEntity) {
            target = (LivingEntity) targetSelector.getEntity();
        }

        if (target == null) {
            @NotNull List<Entity> entities = player.getNearbyEntities(30,30,30);
            LivingEntity looking = null;
            for (Entity e : entities) {
                if (!(e instanceof LivingEntity)){
                    continue;
                }
                if (isEntityLooking(player, (LivingEntity) e)) {
                    looking = (LivingEntity) e;
                    break;
                }
            }
            if (looking == null) {
                SendMessage(player, details(), "あなたはどのエンティティも見ていません...");
                return;
            }
            target = looking;
        }
        if (player.getUniqueId() == target.getUniqueId()) {
            SendMessage(player, details(), "あなたからあなた自身にリードを付けることはできません。");
            return;
        }
        boolean bool = target.setLeashHolder(player);
        SendMessage(player, details(), String.format("ターゲット「%s (%s)」にリードを%s。",
            target.getName(),
            target.getType().name(),
            bool ? "付けました" : "付けられませんでした"));
    }

//    void leadEntityToEntity(CommandContext<CommandSender> context) {
//        CommandSender sender = context.getSender();
//        SingleEntitySelector fromSelector = context.get("from");
//        Entity from = null;
//        if (fromSelector.hasAny()) {
//            from = fromSelector.getEntity();
//        }
//        SingleEntitySelector toSelector = context.get("to");
//        Entity to = null;
//        if (toSelector.hasAny()) {
//            to = toSelector.getEntity();
//        }
//        if (from == null) {
//            SendMessage(sender, details(), "リードを付ける側のプレイヤー・エンティティが指定されていません");
//            return;
//        }
//        if (to == null) {
//            SendMessage(sender, details(), "リードを付けられる側のプレイヤー・エンティティが指定されていません");
//            return;
//        }
//        if (from.getUniqueId() == to.getUniqueId()) {
//            SendMessage(sender, details(), "乗るプレイヤー・エンティティと乗られるプレイヤー・エンティティが同じです。");
//            return;
//        }
//        boolean bool = to.addPassenger(from);
//        SendMessage(sender, details(), String.format("「%s (%s)」から「%s (%s)」にリードを%s。",
//            from.getName(),
//            from.getType().name(),
//            to.getName(),
//            to.getType().name(),
//            bool ? "付けました" : "付けられませんでした"));
//    }
//
//    void leaveEntity(CommandContext<CommandSender> context) {
//        CommandSender sender = context.getSender();
//        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
//        LivingEntity target = null;
//
//        if (targetSelector != null && targetSelector.hasAny() && targetSelector.getEntity() instanceof LivingEntity) {
//            target = (LivingEntity) targetSelector.getEntity();
//        }
//
//        if (target == null) {
//            @NotNull List<Entity> entities = player.getNearbyEntities(30,30,30);
//            LivingEntity looking = null;
//            for (Entity e : entities) {
//                if (!(e instanceof LivingEntity)){
//                    continue;
//                }
//                if (isEntityLooking(player, (LivingEntity) e)) {
//                    looking = (LivingEntity) e;
//                    break;
//                }
//            }
//            if (looking == null) {
//                SendMessage(player, details(), "あなたはどのエンティティも見ていません...");
//                return;
//            }
//            target = looking;
//        }
//        for (Entity entity : target.getPassengers()) {
//            if (target.removePassenger(entity)) {
//                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろしました。", entity.getName(), entity.getType().name()));
//            } else if (entity.getVehicle() != null && entity.getVehicle().eject()) {
//                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろしました。", entity.getName(), entity.getType().name()));
//            } else {
//                SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」を下ろせませんでした。", entity.getName(), entity.getType().name()));
//            }
//        }
//    }
}
