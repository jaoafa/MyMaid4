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
import org.bukkit.entity.EntityType;
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
                .meta(CommandMeta.DESCRIPTION, "1辺30ブロックの立方体内にある指定された名前の[エンティティ]にリードを付けます。プレイヤーは指定できません")
                .literal("set")
                .argument(SingleEntitySelectorArgument.optional("target"),
                    ArgumentDescription.of("対象のエンティティ。指定しない場合見ているエンティティ"))
                .handler(this::leadEntity)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "[プレイヤーもしくはエンティティ(付ける側)]から、[エンティティ(付けられる側)]にリードを付けます。")
                .literal("connect")
                .argument(SingleEntitySelectorArgument.of("from"),
                    ArgumentDescription.of("リードを持っている側のプレイヤー・エンティティ"))
                .argument(SingleEntitySelectorArgument.of("to"),
                    ArgumentDescription.of("リードを付けられる側のエンティティ。プレイヤーは指定できません"))
                .handler(this::leadEntityToEntity)
                .build(),
            builder
                .meta(CommandMeta.DESCRIPTION, "[エンティティ]に付いているリードを外します。")
                .literal("leave")
                .argument(SingleEntitySelectorArgument.optional("target"),
                    ArgumentDescription.of("対象のエンティティ"))
                .handler(this::unLeadEntity)
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

    void leadEntityToEntity(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SingleEntitySelector fromSelector = context.get("from");
        LivingEntity from = null;
        if (fromSelector.hasAny() && fromSelector.getEntity() instanceof LivingEntity) {
            from = (LivingEntity) fromSelector.getEntity();
        }
        SingleEntitySelector toSelector = context.get("to");
        LivingEntity to = null;
        if (toSelector.hasAny() && toSelector.getEntity() instanceof LivingEntity) {
            to = (LivingEntity) toSelector.getEntity();
        }

        if (from == null) {
            SendMessage(sender, details(), "リードを付ける側のプレイヤー・エンティティが指定されていません");
            return;
        }
        if (to == null) {
            SendMessage(sender, details(), "リードを付けられる側のプレイヤー・エンティティが指定されていません");
            return;
        }
        if (from.getUniqueId() == to.getUniqueId()) {
            SendMessage(sender, details(), "リードを付ける側のプレイヤー・エンティティとリードを付けられる側のプレイヤー・エンティティが同じです。");
            return;
        }
        boolean bool = to.setLeashHolder(from);
        SendMessage(sender, details(), String.format("「%s (%s)」から「%s (%s)」にリードを%s。",
            from.getName(),
            from.getType().name(),
            to.getName(),
            to.getType().name(),
            bool ? "付けました" : "付けられませんでした"));
    }

    void unLeadEntity(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
        LivingEntity target = null;

        if (targetSelector != null && targetSelector.hasAny() && targetSelector.getEntity() instanceof LivingEntity) {
            target = (LivingEntity) targetSelector.getEntity();
        }

        if (target == null) {
            if (!(context.getSender() instanceof Player)) {
                SendMessage(context.getSender(), details(), "targetが必要です。");
                return;
            }
            target = (Player) context.getSender();
        }

        if (target.setLeashHolder(null)) {
            SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」からリードを外しました。", target.getName(), target.getType().name()));
        } else {
            SendMessage(sender, details(), String.format("プレイヤー・エンティティ「%s(%s)」からリードを外せませんでした。外そうとしたエンティティがすでに他のエンティティとリードで結ばれている場合は、もう一方のエンティティの方を指定するとリードを外せます。", target.getName(), target.getType().name()));
        }
    }
}