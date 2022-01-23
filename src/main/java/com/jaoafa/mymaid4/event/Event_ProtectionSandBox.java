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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Event_ProtectionSandBox extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "SandBoxをRegular権限グループ未満のプレイヤーが編集できないように制限します。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void ontoSandBox(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();
        if (!toWorld.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxで建築することはできません。");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSandBoxPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxでブロック編集することはできません。");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSandBoxBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxでブロック編集することはできません。");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSandBoxIgniteEvent(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxで着火することはできません。");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSandBoxBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxで水や溶岩を撒くことはできません。");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSandBoxBucketFillEvent(PlayerBucketFillEvent event) {
        Block block = event.getBlockClicked();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxで水や溶岩を撒くことはできません。");
        event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractRight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR ||
            player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            return;
        }

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxに干渉することはできません。");
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractLeft(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR ||
            player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            return;
        }

        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxに干渉することはできません。");
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamageArmorStand(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        World world = player.getWorld();
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) {
            return;
        }
        if (!world.getName().equalsIgnoreCase("SandBox")) {
            return; // SandBoxのみ
        }
        if (isAMR(player)) {
            return; // RMA除外
        }
        player.sendMessage("[SandBox] " + ChatColor.RED + "あなたの権限ではSandBoxでアーマースタンドを壊すことができません。");
        event.setCancelled(true);
    }
}