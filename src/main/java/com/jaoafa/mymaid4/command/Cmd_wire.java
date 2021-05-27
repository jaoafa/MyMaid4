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
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.types.tuples.Triplet;
import com.jaoafa.mymaid4.lib.CommandPremise;
import com.jaoafa.mymaid4.lib.MyMaidCommand;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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

import java.util.Collections;

import static com.jaoafa.mymaid4.Main.getWorldEdit;

public class Cmd_wire extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "wire",
            Collections.singletonList("leadunit"),
            "指定した2点間にリードを張ったり、撤去したりします。"
        );
    }

    @Override
    public MyMaidCommand.Cmd register(Command.Builder<CommandSender> builder) {
        return new MyMaidCommand.Cmd(

            builder
                .meta(CommandMeta.DESCRIPTION, "指定した2点間にリードを張ります。")
                .senderType(Player.class)
                .literal("set", "add", "connect")
                .argumentTriplet(
                    "pos1",
                    TypeToken.get(Vector.class),
                    Triplet.of("x1", "y1", "z1"),
                    Triplet.of(Integer.class, Integer.class, Integer.class), // バニラのようにintで入ってきた場合は+0.5blockし、doubleの場合はそのまま代入するようにしたい
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("1つ目のx・y・z座標。リードを付けられている側。")
                )
                .argumentTriplet(
                    "pos2",
                    TypeToken.get(Vector.class),
                    Triplet.of("x2", "y2", "z2"),
                    Triplet.of(Integer.class, Integer.class, Integer.class), // バニラのようにintで入ってきた場合は+0.5blockし、doubleの場合はそのまま代入するようにしたい
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("2つ目のx・y・z座標。リードを持っている側。")
                )
                .handler(this::setWire)
                .build(),

            builder
                .meta(CommandMeta.DESCRIPTION, "WorldEditで選択した2点間にリードを張ります。1座標目がリードを付けられている側、2座標目がリードを持っている側。")
                .senderType(Player.class)
                .literal("setwe", "addwe", "connectwe", "we")
                .handler(this::setWireWe)
//                .build(),
//
//            builder
//                .meta(CommandMeta.DESCRIPTION, "指定した2点間のリードを撤去します。座標を指定しない場合、WorldEditで選択した2点間のリードを撤去します。")
//                .literal("del", "delete", "remove")
//                .argument(SingleEntitySelectorArgument.of("from"),
//                    ArgumentDescription.of("リードを持っている側のMob（またはプレイヤー）"))
//                .argument(SingleEntitySelectorArgument.of("to"),
//                    ArgumentDescription.of("リードを付けられる側のMob"))
//                .handler(this::delWire)
//                .build(),
//
//            builder
//                .meta(CommandMeta.DESCRIPTION, "WorldEditで選択した2点間にリードを張ります。1座標目がリードを付けられている側、2座標目がリードを持っている側。")
//                .senderType(Player.class)
//                .literal("delwe", "deletewe", "removewe")
//                .handler(this::delWireWe)
//                .build(),
//
//            builder
//                .meta(CommandMeta.DESCRIPTION, "1辺30ブロックの立方体内にいる、どこにも繋がっていないリード接続用コウモリをキルします。不具合対策用です。")
//                .literal("delbat")
//                .argument(SingleEntitySelectorArgument.of("from"),
//                    ArgumentDescription.of("リードを持っている側のMob（またはプレイヤー）"))
//                .argument(SingleEntitySelectorArgument.of("to"),
//                    ArgumentDescription.of("リードを付けられる側のMob"))
//                .handler(this::delWireUnit)
                .build()
        );
    }

    void setWire(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Vector locationArgumentPos1 = context.get("pos1");
        Vector locationArgumentPos2 = context.get("pos2");
        Location loc1 = new Location(player.getWorld(), locationArgumentPos1.getX() + 0.5, locationArgumentPos1.getY(), locationArgumentPos1.getZ() + 0.5);
        Location loc2 = new Location(player.getWorld(), locationArgumentPos2.getX() + 0.5, locationArgumentPos2.getY(), locationArgumentPos2.getZ() + 0.5);

        summonBat(player, loc1, loc2);
    }

    void setWireWe(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        WorldEditPlugin we = getWorldEdit();

        if (we == null) {
            SendMessage(player, details(), "WorldEditと連携できないため、このコマンドを使用できません。");
            return;
        }

        try {
            World selectionWorld = we.getSession(player).getSelectionWorld();
            CuboidRegion cuboidRegion = (CuboidRegion) we.getSession(player).getSelection(selectionWorld);

            BlockVector3 locationArgumentWePos1 = cuboidRegion.getPos1();
            BlockVector3 locationArgumentWePos2 = cuboidRegion.getPos2();
            Location loc1 = new Location(player.getWorld(), locationArgumentWePos1.getX() + 0.5, locationArgumentWePos1.getY(), locationArgumentWePos1.getZ() + 0.5);
            Location loc2 = new Location(player.getWorld(), locationArgumentWePos2.getX() + 0.5, locationArgumentWePos2.getY(), locationArgumentWePos2.getZ() + 0.5);

            summonBat(player, loc1, loc2);

        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");

        }
    }

//    void delWireWe(CommandContext<CommandSender> context) {
//        Player player = (Player) context.getSender();
//
//        WorldEditPlugin we = getWorldEdit();
//
//        if (we == null) {
//            SendMessage(player, details(), "WorldEditと連携できないため、このコマンドを使用できません。");
//            return;
//        }
//
//        try {
//            World selectionWorld = we.getSession(player).getSelectionWorld();
//            CuboidRegion cuboidRegion = (CuboidRegion) we.getSession(player).getSelection(selectionWorld);
//
//            BlockVector3 locationArgumentWePos1 = cuboidRegion.getPos1();
//            BlockVector3 locationArgumentWePos2 = cuboidRegion.getPos2();
//            Location loc1 = new Location(player.getWorld(), locationArgumentWePos1.getX() + 0.5, locationArgumentWePos1.getY(), locationArgumentWePos1.getZ() + 0.5);
//            Location loc2 = new Location(player.getWorld(), locationArgumentWePos2.getX() + 0.5, locationArgumentWePos2.getY(), locationArgumentWePos2.getZ() + 0.5);
//
//            summonBat(player, loc1, loc2);
//
//        } catch (IncompleteRegionException e) {
//            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
//
//        }
//    }

    void delWireUnit(CommandContext<CommandSender> context) {
        CommandSender sender = context.getSender();
        SendMessage(sender, details(), "このコマンドは未実装です。");
    }

    void summonBat(Player player, Location loc1, Location loc2) {

        LivingEntity bat1 = player.getWorld().spawn(loc1, Bat.class, batnbt1 -> {
            batnbt1.setAI(false);
            batnbt1.setAwake(true);
            batnbt1.setSilent(true);
            batnbt1.setInvulnerable(true);
            batnbt1.setRemoveWhenFarAway(false);
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, true, false, true)); //確認用
            batnbt1.addScoreboardTag("wireUnit");
        });

        LivingEntity bat2 = player.getWorld().spawn(loc2, Bat.class, batnbt2 -> {
            batnbt2.setAI(false);
            batnbt2.setAwake(true);
            batnbt2.setSilent(true);
            batnbt2.setInvulnerable(true);
            batnbt2.setRemoveWhenFarAway(false);
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 1, true, false, true)); //確認用
            batnbt2.addScoreboardTag("wireUnit");
        });

        boolean bool = bat2.setLeashHolder(bat1);
        SendMessage(player, details(), String.format("pos1（%s, %s, %s）とpos2（%s, %s, %s）の間にリードを%s。",
            loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
            loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(),
            bool ? "付けました" : "付けられませんでした"));
    }
}