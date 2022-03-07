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
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.jaoafa.mymaid4.Main.getWorldEdit;

public class Cmd_Wire extends MyMaidLibrary implements CommandPremise {
    final static double maxDistance = 128.0;
    final static int searchBatRadius = 2;

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
                .literal("setwe", "addwe", "connectwe")
                .handler(this::setWireWe)
                .build(),

            builder
                .meta(CommandMeta.DESCRIPTION, "指定した2点間のリードを撤去します。")
                .senderType(Player.class)
                .literal("del", "delete", "remove")
                .argumentTriplet(
                    "pos1",
                    TypeToken.get(Vector.class),
                    Triplet.of("x1", "y1", "z1"),
                    Triplet.of(Integer.class, Integer.class, Integer.class), // バニラのようにintで入ってきた場合は+0.5blockし、doubleの場合はそのまま代入するようにしたい
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("1つ目のx・y・z座標。どちら側でも良いです。")
                )
                .argumentTriplet(
                    "pos2",
                    TypeToken.get(Vector.class),
                    Triplet.of("x2", "y2", "z2"),
                    Triplet.of(Integer.class, Integer.class, Integer.class), // バニラのようにintで入ってきた場合は+0.5blockし、doubleの場合はそのまま代入するようにしたい
                    (sender, triplet) -> new Vector(triplet.getFirst(), triplet.getSecond(), triplet.getThird()),
                    ArgumentDescription.of("2つ目のx・y・z座標。どちら側でも良いです。")
                )
                .handler(this::delWire)
                .build(),

            builder
                .meta(CommandMeta.DESCRIPTION, "WorldEditで選択した地点間のリードを撤去します。3地点以上を選択した場合、1-2,2-3,3-4地点間のようにリードが撤去されますが、選択した地点は全て同じ高さである必要があります。")
                .senderType(Player.class)
                .literal("delwe", "deletewe", "removewe")
                .handler(this::delWireWe)
                .build()
        );
    }

    void setWire(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Vector locationArgumentPos1 = context.get("pos1");
        Vector locationArgumentPos2 = context.get("pos2");
        Location loc1 = new Location(player.getWorld(), locationArgumentPos1.getX() + 0.5, locationArgumentPos1.getY() + 0.1725, locationArgumentPos1.getZ() + 0.5);
        Location loc2 = new Location(player.getWorld(), locationArgumentPos2.getX() + 0.5, locationArgumentPos2.getY() + 0.0375, locationArgumentPos2.getZ() + 0.5 - 0.2);

        if (loc1.distance(loc2) > maxDistance) {
            SendMessage(player, details(), String.format("地点間の距離が%smを超えています。%sm以内で指定してください。", maxDistance, maxDistance));
            return;
        }
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

        if (region instanceof CuboidRegion cuboidRegion) {

            BlockVector3 locationArgumentWePos1 = cuboidRegion.getPos1();
            BlockVector3 locationArgumentWePos2 = cuboidRegion.getPos2();
            Location loc1 = new Location(player.getWorld(), locationArgumentWePos1.getX() + 0.5, locationArgumentWePos1.getY() + 0.1725, locationArgumentWePos1.getZ() + 0.5);
            Location loc2 = new Location(player.getWorld(), locationArgumentWePos2.getX() + 0.5, locationArgumentWePos2.getY() + 0.0375, locationArgumentWePos2.getZ() + 0.5 - 0.2);

            if (loc1.distance(loc2) > maxDistance) {
                SendMessage(player, details(), String.format("地点間の距離が%smを超えています。%sm以内で指定してください。", maxDistance, maxDistance));
                return;
            }
            summonBat(player, loc1, loc2, true);

        } else if (region instanceof Polygonal2DRegion polyRegion) {

            int summonCount = 0;

            if (region.getHeight() != 1) {
                SendMessage(player, details(), "3つ以上の座標を指定した場合は、全てのY座標が同じ高さでなければ実行できません。");
                SendMessage(player, details(), "再度選択するか、高さを1ブロックに調整してください。");
                return;
            }

            List<BlockVector2> polylist = polyRegion.getPoints();
            int polyPosY = polyRegion.getMaximumY();

            for (int i = 0; i < polylist.size() - 1; i++) {
                Location loc1 = new Location(player.getWorld(), polylist.get(i).getX() + 0.5, polyPosY + 0.1725, polylist.get(i).getZ() + 0.5);
                Location loc2 = new Location(player.getWorld(), polylist.get(i + 1).getX() + 0.5, polyPosY + 0.0375, polylist.get(i + 1).getZ() + 0.5 - 0.2);
                if (loc1.distance(loc2) > maxDistance) {
                    SendMessage(player, details(), String.format("(%s,%s,%s) - (%s,%s,%s) の距離が%smを超えています。",
                        loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(), maxDistance));
                    SendMessage(player, details(), String.format("%sm以内になるように指定してください。", maxDistance));
                    return;
                }
            }

            for (int i = 0; i < polylist.size() - 1; i++) {
                Location loc1 = new Location(player.getWorld(), polylist.get(i).getX() + 0.5, polyPosY + 0.1725, polylist.get(i).getZ() + 0.5);
                Location loc2 = new Location(player.getWorld(), polylist.get(i + 1).getX() + 0.5, polyPosY + 0.0375, polylist.get(i + 1).getZ() + 0.5 - 0.2);
                summonCount += summonBat(player, loc1, loc2, false);
            }

            SendMessage(player, details(), String.format("選択された座標間にリードを計%s本張りました。", summonCount));
            SendMessage(player, details(), "確認のためコウモリは10秒間発光します。");
        }
    }

    void delWire(CommandContext<CommandSender> context) {
        Player player = (Player) context.getSender();
        Vector locationArgumentPos1 = context.get("pos1");
        Vector locationArgumentPos2 = context.get("pos2");
        Location loc1 = new Location(player.getWorld(), locationArgumentPos1.getX() + 0.5, locationArgumentPos1.getY() + 0.1725, locationArgumentPos1.getZ() + 0.5);
        Location loc2 = new Location(player.getWorld(), locationArgumentPos2.getX() + 0.5, locationArgumentPos2.getY() + 0.0375, locationArgumentPos2.getZ() + 0.5 - 0.2);

        @NotNull Collection<Entity> loc1entities = loc1.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);
        @NotNull Collection<Entity> loc2entities = loc2.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);

        removeBat(loc1entities, loc2entities);
        SendMessage(player, details(), "指定した座標間のリードを撤去する操作を行いました。実際には撤去されていないことがあります。");
    }

    void delWireWe(CommandContext<CommandSender> context) {
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

        if (region instanceof CuboidRegion cuboidRegion) {

            BlockVector3 locationArgumentWePos1 = cuboidRegion.getPos1();
            BlockVector3 locationArgumentWePos2 = cuboidRegion.getPos2();
            Location loc1 = new Location(player.getWorld(), locationArgumentWePos1.getX() + 0.5, locationArgumentWePos1.getY() + 0.1725, locationArgumentWePos1.getZ() + 0.5);
            Location loc2 = new Location(player.getWorld(), locationArgumentWePos2.getX() + 0.5, locationArgumentWePos2.getY() + 0.0375, locationArgumentWePos2.getZ() + 0.5 - 0.2);
            @NotNull Collection<Entity> loc1entities = loc1.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);
            @NotNull Collection<Entity> loc2entities = loc2.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);

            removeBat(loc1entities, loc2entities);
            SendMessage(player, details(), "指定した座標間のリードを撤去する操作を行いました。実際には撤去されていないことがあります。");

        } else if (region instanceof Polygonal2DRegion polyRegion) {

            if (region.getHeight() != 1) {
                SendMessage(player, details(), "3つ以上の座標を指定した場合は、全てのY座標が同じ高さでなければ実行できません。");
                SendMessage(player, details(), "再度選択するか、高さを1ブロックに調整してください。");
                return;
            }

            List<BlockVector2> polylist = polyRegion.getPoints();
            int polyPosY = polyRegion.getMaximumY();
            int wireRemoveCount = 0;

            for (int i = 0; i < polylist.size() - 1; i++) {
                Location loc1 = new Location(player.getWorld(), polylist.get(i).getX() + 0.5, polyPosY + 0.1725, polylist.get(i).getZ() + 0.5);
                Location loc2 = new Location(player.getWorld(), polylist.get(i + 1).getX() + 0.5, polyPosY + 0.0375, polylist.get(i + 1).getZ() + 0.5 - 0.2);
                @NotNull Collection<Entity> loc1entities = loc1.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);
                @NotNull Collection<Entity> loc2entities = loc2.getNearbyEntities(searchBatRadius, searchBatRadius, searchBatRadius);

                removeBat(loc1entities, loc2entities);
            }
            SendMessage(player, details(), "指定した座標間のリードを撤去する操作を行いました。実際には撤去されていないことがあります。");
        }
    }

    void removeBat(Collection<Entity> locAentities, Collection<Entity> locBentities) {

        for (Entity locAe : locAentities) {
            if (locAe.getType() != EntityType.BAT) continue; // コウモリではない
            if (!locAe.getScoreboardTags().contains("CmdWire1") && !locAe.getScoreboardTags().contains("CmdWire2"))
                continue; // CmdWire1とCmdWire2、どちらのタグもついてない

            for (Entity locBe : locBentities) {
                if (locBe.getType() != EntityType.BAT) continue; // コウモリではない
                if (!locBe.getScoreboardTags().contains("CmdWire1") && !locBe.getScoreboardTags().contains("CmdWire2"))
                    continue; // CmdWire1とCmdWire2、どちらのタグもついてない

                if (((LivingEntity) locAe).isLeashed() && ((LivingEntity) locAe).getLeashHolder() == locBe) {
                    locAe.remove();
                    locBe.remove();
                }
                if (((LivingEntity) locBe).isLeashed() && ((LivingEntity) locBe).getLeashHolder() == locAe) {
                    locAe.remove();
                    locBe.remove();
                }
            }
        }
    }

    int summonBat(Player player, Location loc1, Location loc2, boolean isNotify) {

        int summonCount = 0;

        LivingEntity bat1 = player.getWorld().spawn(loc1, Bat.class, batnbt1 -> {
            batnbt1.setAI(false);
            batnbt1.setAwake(true);
            batnbt1.setSilent(true);
            batnbt1.setInvulnerable(true);
            batnbt1.setRemoveWhenFarAway(false);
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false, true)); //確認用
            batnbt1.addScoreboardTag("CmdWire1");
        });

        LivingEntity bat2 = player.getWorld().spawn(loc2, Bat.class, batnbt2 -> {
            batnbt2.setAI(false);
            batnbt2.setAwake(true);
            batnbt2.setSilent(true);
            batnbt2.setInvulnerable(true);
            batnbt2.setRemoveWhenFarAway(false);
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
            batnbt2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, true, false, true)); //確認用
            batnbt2.addScoreboardTag("CmdWire2");
        });

        boolean bool = bat2.setLeashHolder(bat1);
        if (bool) {
            summonCount++;
        }

        if (isNotify) {
            SendMessage(player, details(), String.format("(%s,%s,%s) - (%s,%s,%s) にリードを%s",
                loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ(),
                bool ? "付けました。" : "付けられませんでした。"));
            SendMessage(player, details(), "確認のためコウモリは10秒間発光します。");
        }
        return summonCount;
    }
}