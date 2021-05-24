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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.types.tuples.Triplet;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;

import static com.jaoafa.mymaid4.Main.getWorldEdit;

public class Cmd_Wire extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "Wire",
            Arrays.asList("leadUnit"),
            "指定した2点間にリードを張ったり、撤去したりします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(

            builder
                .meta(CommandMeta.DESCRIPTION, "指定した2点間にリードを張ります。座標を指定しない場合、WorldEditで選択した2点間にリードを張ります。")
                .literal("set", "add", "connect")
                .argumentTriplet(
                    "coords1",
                    TypeToken.get(Vector.class),
                    Triplet.of("x1", "y1", "z1"),
                    Triplet.of(Integer.class, Integer.class, Integer.class),
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("Coordinates")
                )
                .argumentTriplet(
                    "coords2",
                    TypeToken.get(Vector.class),
                    Triplet.of("x2", "y2", "z2"),
                    Triplet.of(Integer.class, Integer.class, Integer.class),
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("Coordinates")
                )
                .handler(this::SetWire)
                .build(),

            builder
                .meta(CommandMeta.DESCRIPTION, "指定した2点間にリードを張ります。座標を指定しない場合、WorldEditで選択した2点間にリードを張ります。")
                .literal("we")
                .handler(this::SetWireWe)
//                .build(),
//
//            builder
//                .meta(CommandMeta.DESCRIPTION, "指定した2点間のリードを撤去します。座標を指定しない場合、WorldEditで選択した2点間のリードを撤去します。")
//                .literal("del", "delete", "remove")
//                .argument(SingleEntitySelectorArgument.of("from"),
//                    ArgumentDescription.of("リードを持っている側のMob（またはプレイヤー）"))
//                .argument(SingleEntitySelectorArgument.of("to"),
//                    ArgumentDescription.of("リードを付けられる側のMob"))
//                .handler(this::unLeadUnit)
//                .build(),
//
//            builder
//                .meta(CommandMeta.DESCRIPTION, "1辺30ブロックの立方体内にいる、どこにも繋がっていないリード接続用コウモリをキルします。不具合対策用です。")
//                .literal("delbat")
//                .argument(SingleEntitySelectorArgument.of("from"),
//                    ArgumentDescription.of("リードを持っている側のMob（またはプレイヤー）"))
//                .argument(SingleEntitySelectorArgument.of("to"),
//                    ArgumentDescription.of("リードを付けられる側のMob"))
//                .handler(this::killLeadUnit)
                .build()
        );
    }

    void SetWire(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        final Vector locationArgumentPos1 = context.get("coords1");
        final Vector locationArgumentPos2 = context.get("coords2");
        SendMessage(player, details(), String.format("TEST座標：pos1(%s,%s,%s)とpos2(%s,%s,%s)",
            locationArgumentPos1.getX(), locationArgumentPos1.getY(), locationArgumentPos1.getZ(),
            locationArgumentPos2.getX(), locationArgumentPos2.getY(), locationArgumentPos2.getZ()));

        SendMessage(player, details(), String.format("プレイヤーのいるワールド：%s", player.getWorld()));

        Location loc1 = new Location(player.getWorld(), locationArgumentPos1.getX(), locationArgumentPos1.getY(), locationArgumentPos1.getZ());
        Location loc2 = new Location(player.getWorld(), locationArgumentPos2.getX(), locationArgumentPos2.getY(), locationArgumentPos2.getZ());
        SendMessage(player, details(), String.format("TEST座標：pos(%s)", loc1));

        LivingEntity bat1 = player.getWorld().spawn(loc1, Bat.class, batnbt1 -> {
            batnbt1.setAI(false);
            batnbt1.setSilent(true);
            batnbt1.setPersistent(true);
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, true, false, true)); //確認用
            batnbt1.addScoreboardTag("wireUnit");
        });

        LivingEntity bat2 = player.getWorld().spawn(loc2, Bat.class, batnbt2 -> {
            batnbt2.setAI(false);
            batnbt2.setSilent(true);
            batnbt2.setPersistent(true);
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, true, false, true));
            batnbt2.addScoreboardTag("wireUnit");
        });

        boolean bool = bat2.setLeashHolder(bat1);
        SendMessage(player, details(), String.format("「%s (%s)」から「%s (%s)」にリードを%s。",
            bat1.getName(),
            bat1.getType().name(),
            bat2.getName(),
            bat2.getType().name(),
            bool ? "付けました" : "付けられませんでした"));

//        Entity bat1 = player.getWorld().spawnEntity(loc1, EntityType.BAT);
//        ((LivingEntity) bat1).setAI(false);

    }

    void SetWireWe(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        WorldEditPlugin we = getWorldEdit();
        if (we == null) {
            SendMessage(player, details(), "WorldEditプラグインが動作していないため、このコマンドを使用できません。");
            return;
        }

        try {
            World selectionWorld = we.getSession(player).getSelectionWorld();
            Region region = we.getSession(player).getSelection(selectionWorld);
            region.getMaximumPoint();
            SendMessage(player, details(), String.format("region:%sは%s,%s,%sと%s,%s,%s",
                region,
                region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ(),
                region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ()));
            SendMessage(player, details(), "WorldEditで指定した範囲に電線を張ったかもしれない");

        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
        }

        SendMessage(player, details(), String.format("TEST座標：pos(%s)", we));
    }

/*    void SetWire(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        player.getWorldspawnEntity(l, EntityType.ARMOR_STAND);

        final Vector locationArgumentPos1 = context.get("coords1");
        final Vector locationArgumentPos2 = context.get("coords2");
        Location loc = new Location(world, 0, 0, 0, 0, 0);

        SingleEntitySelector targetSelector = context.getOrDefault("target", null);
        LivingEntity target = null;

        if (targetSelector != null && targetSelector.hasAny() && targetSelector.getEntity() instanceof LivingEntity) {
            target = (LivingEntity) targetSelector.getEntity();
        }

        if (target == null) {
            @NotNull List<Entity> entities = player.getNearbyEntities(30, 30, 30);
            LivingEntity looking = null;
            for (Entity e : entities) {
                if (!(e instanceof LivingEntity)) {
                    continue;
                }
                if (isEntityLooking(player, (LivingEntity) e)) {
                    looking = (LivingEntity) e;
                    break;
                }
            }
            if (looking == null) {
                SendMessage(player, details(), "あなたはどのMobも見ていません...");
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
    }*/

    void DelWire(CommandContext<CommandSender> context) {
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
            SendMessage(sender, details(), "リードを付ける側のMob（またはプレイヤー）が指定されていません");
            return;
        }
        if (to == null) {
            SendMessage(sender, details(), "リードを付けられる側のMobが指定されていません");
            return;
        }
        if (from.getUniqueId() == to.getUniqueId()) {
            SendMessage(sender, details(), "リードを付ける側のMob（またはプレイヤー）と、リードを付けられる側のMob（またはプレイヤー）が同じです。");
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

    void DelWireUnit(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SendMessage(sender, details(), "このコマンドは未実装です。");
    }
}