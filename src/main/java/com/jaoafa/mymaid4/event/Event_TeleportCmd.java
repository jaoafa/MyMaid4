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

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.customEvents.TeleportCommandEvent;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;
import java.util.Optional;

public class Event_TeleportCmd extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "tpコマンドを受けてTeleportCommandEventを動作させます。";
    }

    /*
    1 /teleport <destination>
    2 /teleport <targets> <destination>
    3 /teleport <x>       <y>           <z>
    4 /teleport <targets> <locX>        <locY> <locZ>
    6 /teleport <targets> <locX>        <locY> <locZ> <yaw>  <pitch>
    7 /teleport <targets> <locX>        <locY> <locZ> facing entity    <facingEntity>
    8 /teleport <targets> <locX>        <locY> <locZ> facing <facingX> <facingY>      <facingZ>
    8 /teleport <targets> <locX>        <locY> <locZ> facing entity    <facingEntity> <facingAnchor>

    targets: テレポートさせるターゲット
    destination: テレポート先のエンティティ
    locX, locY, locZ, yaw, pitch: テレポート先の座標
    facingX, facingY, facingZ: ターゲットが向く座標
    facingEntity: ターゲットが向くエンティティ
    facingAnchor: エンティティの目・足どちらを向くか (eyes, feetのいずれか)

    /tp はエイリアス
     */

    @EventHandler
    public void onTeleportCommandFromPlayer(PlayerCommandPreprocessEvent event) {
        Player sender = event.getPlayer();
        String command = event.getMessage();
        String[] args = command.split(" ");
        if (args.length == 0) {
            return; // 本来発生しないと思うけど
        }
        if (!args[0].equalsIgnoreCase("/tp") &&
            !args[0].equalsIgnoreCase("/minecraft:tp") &&
            !args[0].equalsIgnoreCase("/teleport") &&
            !args[0].equalsIgnoreCase("/minecraft:teleport")) {
            return; // tpコマンド以外
        }

        if (args.length == 2) {
            // 1 /teleport <destination>
            Player toPlayer = Bukkit.getPlayerExact(args[1]);
            if (toPlayer == null) return;

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, toPlayer);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 3) {
            // 2 /teleport <targets> <destination>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            Player toPlayer = Bukkit.getPlayerExact(args[2]);
            if (fromPlayer == null) return;
            if (toPlayer == null) return;

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, toPlayer);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 4) {
            // 3 /teleport <x> <y> <z>
            if (!isInt(args[1]) || !isInt(args[2]) || !isInt(args[3])) {
                return;
            }
            int x = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);
            int z = Integer.parseInt(args[3]);
            Location loc = new Location(sender.getWorld(), x, y, z);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, loc);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 5) {
            // 4 /teleport <targets> <locX> <locY> <locZ>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            Location loc = new Location(fromPlayer.getWorld(), x, y, z);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 7) {
            // 6 /teleport <targets> <locX> <locY> <locZ> <yaw> <pitch>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4]) || !isInt(args[5]) || !isInt(args[6])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            int yaw = Integer.parseInt(args[5]);
            int pitch = Integer.parseInt(args[6]);
            Location loc = new Location(sender.getWorld(), x, y, z, yaw, pitch);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 8) {
            // 7 /teleport <targets> <locX> <locY> <locZ> facing entity <facingEntity>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4]) || !isInt(args[5])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            if (!args[5].equals("facing")) return;
            if (!args[6].equals("entity")) return;
            Player facingPlayer = Bukkit.getPlayerExact(args[7]);
            if (facingPlayer == null) return;
            Location loc = new Location(sender.getWorld(), x, y, z);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingPlayer);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 9) {
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            Location loc = new Location(sender.getWorld(), x, y, z);
            String facing = args[5];
            if (!facing.equals("facing")) return;
            if (isInt(args[6]) || isInt(args[7]) || isInt(args[8])) {
                // 8 /teleport <targets> <locX> <locY> <locZ> facing <facingX> <facingY> <facingZ>
                int facingX = Integer.parseInt(args[6]);
                int facingY = Integer.parseInt(args[7]);
                int facingZ = Integer.parseInt(args[8]);
                Location facingLoc = new Location(sender.getWorld(), facingX, facingY, facingZ);

                TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingLoc);
                Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
                if (tpCmdEvent.isCancelled()) event.setCancelled(true);
            } else if (args[6].equals("entity")) {
                // 8 /teleport <targets> <locX> <locY> <locZ> facing entity <facingEntity> <facingAnchor>
                Player facingPlayer = Bukkit.getPlayerExact(args[7]);
                if (facingPlayer == null) return;
                Optional<TeleportCommandEvent.FacingAnchor> facingAnchor = Arrays.stream(TeleportCommandEvent.FacingAnchor.values())
                    .filter(s -> s.name().equalsIgnoreCase(args[8]))
                    .findFirst();
                if (facingAnchor.isEmpty()) return;

                TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingPlayer, facingAnchor.get());
                Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
                if (tpCmdEvent.isCancelled()) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTeleportCommandFromNonPlayer(ServerCommandEvent event) {
        CommandSender sender = event.getSender();
        String command = event.getCommand();
        World world = sender instanceof BlockCommandSender ?
            ((BlockCommandSender) sender).getBlock().getWorld() :
            Bukkit.getServer().getWorlds().get(0);
        String[] args = command.split(" ");
        if (args.length == 0) {
            return; // 本来発生しないと思うけど
        }
        if (!args[0].equalsIgnoreCase("tp") &&
            !args[0].equalsIgnoreCase("minecraft:tp") &&
            !args[0].equalsIgnoreCase("teleport") &&
            !args[0].equalsIgnoreCase("minecraft:teleport") &&
            !args[0].equalsIgnoreCase("/tp") &&
            !args[0].equalsIgnoreCase("/minecraft:tp") &&
            !args[0].equalsIgnoreCase("/teleport") &&
            !args[0].equalsIgnoreCase("/minecraft:teleport")) {
            return; // tpコマンド以外
        }

        if (args.length == 3) {
            // 2 /teleport <targets> <destination>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            Player toPlayer = Bukkit.getPlayerExact(args[2]);
            if (fromPlayer == null) return;
            if (toPlayer == null) return;

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, toPlayer);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 5) {
            // 4 /teleport <targets> <locX> <locY> <locZ>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            Location loc = new Location(fromPlayer.getWorld(), x, y, z);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 7) {
            // 6 /teleport <targets> <locX> <locY> <locZ> <yaw> <pitch>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4]) || !isInt(args[5]) || !isInt(args[6])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            int yaw = Integer.parseInt(args[5]);
            int pitch = Integer.parseInt(args[6]);
            Location loc = new Location(fromPlayer.getWorld(), x, y, z, yaw, pitch);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 8) {
            // 7 /teleport <targets> <locX> <locY> <locZ> facing entity <facingEntity>
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4]) || !isInt(args[5])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            if (!args[5].equals("facing")) return;
            if (!args[6].equals("entity")) return;
            Player facingPlayer = Bukkit.getPlayerExact(args[7]);
            if (facingPlayer == null) return;
            Location loc = new Location(world, x, y, z);

            TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingPlayer);
            Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
            if (tpCmdEvent.isCancelled()) event.setCancelled(true);
        } else if (args.length == 9) {
            Player fromPlayer = Bukkit.getPlayerExact(args[1]);
            if (fromPlayer == null) return;
            if (!isInt(args[2]) || !isInt(args[3]) || !isInt(args[4])) {
                return;
            }
            int x = Integer.parseInt(args[2]);
            int y = Integer.parseInt(args[3]);
            int z = Integer.parseInt(args[4]);
            Location loc = new Location(world, x, y, z);
            String facing = args[5];
            if (!facing.equals("facing")) return;
            if (isInt(args[6]) || isInt(args[7]) || isInt(args[8])) {
                // 8 /teleport <targets> <locX> <locY> <locZ> facing <facingX> <facingY> <facingZ>
                int facingX = Integer.parseInt(args[6]);
                int facingY = Integer.parseInt(args[7]);
                int facingZ = Integer.parseInt(args[8]);
                Location facingLoc = new Location(world, facingX, facingY, facingZ);

                TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingLoc);
                Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
                if (tpCmdEvent.isCancelled()) event.setCancelled(true);
            } else if (args[6].equals("entity")) {
                // 8 /teleport <targets> <locX> <locY> <locZ> facing entity <facingEntity> <facingAnchor>
                Player facingPlayer = Bukkit.getPlayerExact(args[7]);
                if (facingPlayer == null) return;
                Optional<TeleportCommandEvent.FacingAnchor> facingAnchor = Arrays.stream(TeleportCommandEvent.FacingAnchor.values())
                    .filter(s -> s.name().equalsIgnoreCase(args[8]))
                    .findFirst();
                if (facingAnchor.isEmpty()) return;

                TeleportCommandEvent tpCmdEvent = new TeleportCommandEvent(sender, fromPlayer, loc, facingPlayer, facingAnchor.get());
                Bukkit.getServer().getPluginManager().callEvent(tpCmdEvent);
                if (tpCmdEvent.isCancelled()) event.setCancelled(true);
            }
        }
    }
}
