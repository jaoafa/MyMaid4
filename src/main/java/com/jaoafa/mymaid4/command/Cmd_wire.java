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
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
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

import java.util.Collections;
import java.util.List;

import static com.jaoafa.mymaid4.Main.getWorldEdit;

public class Cmd_wire extends MyMaidLibrary implements CommandPremise {
    @Override
    public MyMaidCommand.Detail details() {
        return new MyMaidCommand.Detail(
            "wire",
            Collections.singletonList("leadunit"),
            "指定した地点間にリードを張ったり、撤去したりします。"
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
                .meta(CommandMeta.DESCRIPTION, "WorldEditで選択した地点間にリードを張ります。3地点以上を選択した場合、1-2,2-3,3-4地点間のようにリードが張られますが、選択した地点は全て同じ高さである必要があります。")
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

        summonBat(player, loc1, loc2, true);
    }

    void setWireWe(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();

        WorldEditPlugin we = getWorldEdit();

        if (we == null) {
            SendMessage(player, details(), "WorldEditと連携できないため、このコマンドを使用できません。");
            return;
        }

        World selectionWorld = we.getSession(player).getSelectionWorld();

        Region region;

        try {
            region = we.getSession(player).getSelection(selectionWorld);

        } catch (IncompleteRegionException e) {
            SendMessage(player, details(), "WorldEditで範囲を指定してください。");
            return;
        }

        if (region instanceof CuboidRegion) {

            CuboidRegion cuboidRegion = (CuboidRegion) region;

            BlockVector3 locationArgumentWePos1 = cuboidRegion.getPos1();
            BlockVector3 locationArgumentWePos2 = cuboidRegion.getPos2();
            Location loc1 = new Location(player.getWorld(), locationArgumentWePos1.getX() + 0.5, locationArgumentWePos1.getY(), locationArgumentWePos1.getZ() + 0.5);
            Location loc2 = new Location(player.getWorld(), locationArgumentWePos2.getX() + 0.5, locationArgumentWePos2.getY(), locationArgumentWePos2.getZ() + 0.5);

            summonBat(player, loc1, loc2, true);

        } else if (region instanceof Polygonal2DRegion) {

            if (region.getHeight() != 1) {
                SendMessage(player, details(), "3つ以上の座標を指定した場合は、全てのY座標が同じ高さでなければ実行できません。再度選択するか、高さを1ブロックに調整してください");
                return;
            }

            Polygonal2DRegion polyRegion = (Polygonal2DRegion) region;
            SendMessage(player, details(), String.format("polyRegion：%s", polyRegion));

            List<BlockVector2> polylist = polyRegion.getPoints();

            int polyPosY = polyRegion.getMaximumY();

            for (int i = 0; i < polylist.size() - 1; i++) {
                Location loc1 = new Location(player.getWorld(), polylist.get(i).getX() + 0.5, polyPosY, polylist.get(i).getZ() + 0.5);
                Location loc2 = new Location(player.getWorld(), polylist.get(i + 1).getX() + 0.5, polyPosY, polylist.get(i + 1).getZ() + 0.5);
                summonBat(player, loc1, loc2, false);
            }

            SendMessage(player, details(), "指定された座標間にリードを張る動作をしました。確認のためコウモリは10秒間発光します。");
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

    void summonBat(Player player, Location loc1, Location loc2, boolean isNotify) {

        LivingEntity bat1 = player.getWorld().spawn(loc1, Bat.class, batnbt1 -> {
            batnbt1.setAI(false);
            batnbt1.setAwake(true);
            batnbt1.setSilent(true);
            batnbt1.setInvulnerable(true);
            batnbt1.setRemoveWhenFarAway(false);
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false, true)); //確認用
            batnbt1.addScoreboardTag("wireUnit");
        });

        LivingEntity bat2 = player.getWorld().spawn(loc2, Bat.class, batnbt2 -> {
            batnbt2.setAI(false);
            batnbt2.setAwake(true);
            batnbt2.setSilent(true);
            batnbt2.setInvulnerable(true);
            batnbt2.setRemoveWhenFarAway(false);
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false, true)); //確認用
            batnbt2.addScoreboardTag("wireUnit");
        });

        boolean bool = bat2.setLeashHolder(bat1);

        if (isNotify) {
            SendMessage(player, details(), String.format("pos1（%s, %s, %s）とpos2（%s, %s, %s）の間にリードを%s。",
                loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
                loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(),
                bool ? "付けました。確認のためコウモリは10秒間発光します" : "付けられませんでした"));
        }
    }
}