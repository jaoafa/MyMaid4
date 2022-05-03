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

import com.jaoafa.jaosuperachievement2.api.Achievementjao;
import com.jaoafa.jaosuperachievement2.lib.Achievement;
import com.jaoafa.mymaid4.Main;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.Jail;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Event_Jail implements Listener, EventPremise {
    @Override
    public String description() {
        return "Jailに関する各種処理を行います。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnEvent_LoginJailCheck(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!MyMaidData.isMainDBActive()) {
            return;
        }

        new BukkitRunnable() {
            public void run() {
                Jail jail = Jail.getInstance(player);
                if (!jail.isStatus()) {
                    return;
                }
                String reason = jail.getReason();
                if (reason == null) {
                    return;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!MyMaidLibrary.isAMR(p)) {
                        continue;
                    }
                    p.sendMessage(
                        String.format("[Jail] %sプレイヤー「%s」は、「%s」という理由でJailされています。", ChatColor.GREEN, player.getName(), reason));
                    p.sendMessage(
                        String.format("[Jail] %s詳しい情報は /jail status %s でご確認ください。", ChatColor.GREEN, player.getName()));
                }
                player.sendMessage(String.format("[Jail] %sあなたは、「%s」という理由でJailされています。", ChatColor.GREEN, reason));
                player.sendMessage(String.format("[Jail] %s解除申請の方法や、Banの方針などは以下ページをご覧ください。", ChatColor.GREEN));
                player.sendMessage(String.format("[Jail] %shttps://jaoafa.com/rule/management/punishment", ChatColor.GREEN));
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) { // 南の楽園外に出られるかどうか
        if (!MyMaidData.isMainDBActive()) {
            return;
        }

        Location to = event.getTo();
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        World world = Bukkit.getServer().getWorld("Jao_Afa");
        if (world == null) {
            return;
        }
        Location prison = MyMaidData.paradiseLocation;
        if (!player.getLocation().getWorld().getUID().equals(world.getUID())) {
            player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたは南の楽園から出られません！");
            // ワールド違い

            if (!MyMaidLibrary.teleportToParadise(player)) {
                // 失敗時
                Location oldBed = player.getBedSpawnLocation();
                player.setBedSpawnLocation(prison, true);
                player.setHealth(0);
                player.setBedSpawnLocation(oldBed, true);
            }
            return;
        }
        double distance = prison.distance(to);
        if (distance >= 65D || to.getBlockY() < 65) {
            // 中央からの距離が65ブロック or y値が65未満
            UUID uuid = player.getUniqueId();
            if (!Jail.hasWarned.containsKey(uuid) || !Jail.hasWarned.get(uuid))
                player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたは南の楽園から出られません！");
            Jail.hasWarned.put(uuid, true);

            if (distance >= 70D) {
                if (!MyMaidLibrary.teleportToParadise(player)) {
                    // 失敗時
                    Location oldBed = player.getBedSpawnLocation();
                    player.setBedSpawnLocation(prison, true);
                    player.setHealth(0);
                    player.setBedSpawnLocation(oldBed, true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }

        event.setRespawnLocation(MyMaidData.paradiseLocation);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getLocation().getWorld().getName().equalsIgnoreCase("Jao_Afa")) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたはブロックを置けません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたはブロックを置けません。");
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたはブロックを壊せません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたはブロックを壊せません。");
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたはブロックを着火できません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたはブロックを着火できません。");
    }

    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたは水や溶岩を撒けません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたは水や溶岩を撒けません。");
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたは水や溶岩を掬うことはできません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたは水や溶岩を掬うことはできません。");
    }

    @EventHandler
    public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        String command = event.getMessage();
        String[] args = command.split(" ", 0);
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("/testment")) {
                return;
            }
        }
        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("/jail") && args[1].equalsIgnoreCase("testment")) {
                return;
            }
        }
        event.setCancelled(true);
        player.sendMessage("[Jail] " + ChatColor.GREEN + "あなたはコマンドを実行できません。");
        Bukkit.getLogger().info("[Jail] " + player.getName() + "==>あなたはコマンドを実行できません。");
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Jail jail = Jail.getInstance(player);
        if (!jail.isStatus()) { // Jailされてる
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return; // 空気に対するアクションは無視
        }
        if (Jail.actionWhitelist.stream().noneMatch(action -> action.action() == event.getAction() && action.checker().check(event))) {
            return; // アクションがホワイトリストにない場合は無視
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoinClearCache(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                Jail.getInstance(player, true);
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                Jail jail = Jail.getInstance(player);
                if (!jail.isStatus()) {
                    return;
                }
                Achievementjao.getAchievementAsync(player, Achievement.JAILBREAKER);
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}

