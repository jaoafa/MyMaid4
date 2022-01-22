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

package com.jaoafa.mymaid4.event;

import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class Event_ProtectionOldWorlds extends MyMaidLibrary implements Listener, EventPremise {
    final String[] worldNames = new String[]{
        "kassi-hp-tk",
        "Jao_Afa_1",
        "Jao_Afa_2",
        "Jao_Afa_3",
        "Jao_Afa_nether_1",
        "Jao_Afa_nether_2",
        "SandBox_1",
        "SandBox_2",
        "SandBox_3",
        "ReJao_Afa",
        "Summer2017",
        "Summer2018",
        "Summer2020",
        "jaoTest1.18.1"
    };
    final Material[] ignoreClickCancel = new Material[]{};
    final Material[] ignoreTargetClickCancel = new Material[]{
        Material.ACACIA_DOOR,
        Material.BIRCH_DOOR,
        Material.DARK_OAK_DOOR,
        Material.IRON_DOOR,
        Material.JUNGLE_DOOR,
        Material.SPRUCE_DOOR,
        Material.IRON_DOOR,
        Material.OAK_DOOR,
        Material.SPRUCE_DOOR,
        Material.BIRCH_DOOR,
        Material.JUNGLE_DOOR,
        Material.ACACIA_DOOR,
        Material.DARK_OAK_DOOR,
        Material.CRIMSON_DOOR,
        Material.WARPED_DOOR,
        Material.STONE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON,
        Material.BIRCH_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.ACACIA_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.CRIMSON_BUTTON,
        Material.WARPED_BUTTON,
        Material.POLISHED_BLACKSTONE_BUTTON,
        Material.CHEST,
        Material.TRAPPED_CHEST,
        Material.STONE_PRESSURE_PLATE,
        Material.OAK_PRESSURE_PLATE,
        Material.SPRUCE_PRESSURE_PLATE,
        Material.BIRCH_PRESSURE_PLATE,
        Material.JUNGLE_PRESSURE_PLATE,
        Material.ACACIA_PRESSURE_PLATE,
        Material.DARK_OAK_PRESSURE_PLATE,
        Material.CRIMSON_PRESSURE_PLATE,
        Material.WARPED_PRESSURE_PLATE,
        Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
        Material.COMMAND_BLOCK,
    };

    @Override
    public String description() {
        return "旧ワールドへの干渉禁止処理を行います。";
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        World world = event.getBlock().getWorld();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでのブロック設置は許可されていません。", NamedTextColor.GREEN)
        ));
        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        World world = event.getBlock().getWorld();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでのブロック破壊は許可されていません。", NamedTextColor.GREEN)
        ));
        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        World world = event.getBlock().getWorld();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでのブロック着火は許可されていません。", NamedTextColor.GREEN)
        ));
        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでの液体撒きは許可されていません。", NamedTextColor.GREEN)
        ));
        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでの液体掬いは許可されていません。", NamedTextColor.GREEN)
        ));
        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST,
                  ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();
        if (!Arrays.asList(worldNames).contains(world.getName())) {
            return;
        }
        if (event.getItem() != null) {
            if (Arrays.asList(ignoreClickCancel).contains(event.getItem().getType())) {
                return;
            }
        }
        if (event.getClickedBlock() != null) {
            if (Arrays.asList(ignoreTargetClickCancel).contains(event.getClickedBlock().getType())) {
                return;
            }
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection]"),
            Component.space(),
            Component.text("旧ワールドでのインタラクトは許可されていません。インタラクトが必要な場合は運営に以下のメッセージのスクリーンショットを提示し除外するよう申請してください。", NamedTextColor.GREEN)
        ));

        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection-DEBUG]"),
            Component.space(),
            Component.text(String.format("Location: %s %d %d %d", world.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), NamedTextColor.GREEN)
        ));
        if (event.getItem() != null) {
            player.sendMessage(Component.text().append(
                Component.text("[OldWorldProtection-DEBUG]"),
                Component.space(),
                Component.text(String.format("event.getItem(): %s", event.getItem().getType().name()), NamedTextColor.GREEN)
            ));
        }
        if (event.getClickedBlock() != null) {
            player.sendMessage(Component.text().append(
                Component.text("[OldWorldProtection-DEBUG]"),
                Component.space(),
                Component.text(String.format("event.getClickedBlock(): %s", event.getClickedBlock().getType().name()), NamedTextColor.GREEN)
            ));
        }
        player.sendMessage(Component.text().append(
            Component.text("[OldWorldProtection-DEBUG]"),
            Component.space(),
            Component.text(String.format("MyMaid4 Version: %s", Main.getMain().getDescription().getVersion()), NamedTextColor.GREEN)
        ));

        if (isA(player)) {
            return;
        }
        event.setCancelled(true);
    }
}
