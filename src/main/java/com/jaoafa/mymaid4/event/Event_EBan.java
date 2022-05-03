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
import com.jaoafa.mymaid4.lib.*;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

public class Event_EBan implements Listener, EventPremise {
    @Override
    public String description() {
        return "EBanに関する各種処理を行います。";
    }

    @EventHandler
    public void onEvent_ChatLiquidBounce(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component component = event.message();
        String message = PlainTextComponentSerializer.plainText().serialize(component);
        EBan eban = EBan.getInstance(player);

        if (!eban.isStatus()) return;
        if (!message.contains("LiquidBounce Client | liquidbounce.net")) return;

        eban.addBan("jaotan", "禁止クライアント「LiquidBounce」使用の疑い。");

        ChatBan chatban = ChatBan.getInstance(player);
        chatban.addBan("jaotan", "禁止クライアント「LiquidBounce」使用の疑い。");

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnEvent_LoginEBanCheck(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!MyMaidData.isMainDBActive()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                EBan eban = EBan.getInstance(player);
                if (!eban.isStatus()) return;
                String reason = eban.getReason();
                if (reason == null) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!MyMaidLibrary.isAMR(p)) continue;
                    p.sendMessage(
                        String.format("[EBan] %sプレイヤー「%s」は、「%s」という理由でEBanされています。", ChatColor.GREEN, player.getName(), reason));
                    p.sendMessage(
                        String.format("[EBan] %s詳しい情報は /eban status %s でご確認ください。", ChatColor.GREEN, player.getName()));
                }
                player.sendMessage(String.format("[EBan] %sあなたは、「%s」という理由でEBanされています。", ChatColor.GREEN, reason));
                player.sendMessage(String.format("[EBan] %s解除申請の方法や、Banの方針などは以下ページをご覧ください。", ChatColor.GREEN));
                player.sendMessage(String.format("[EBan] %shttps://jaoafa.com/rule/management/punishment", ChatColor.GREEN));
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) { // 南の楽園外に出られるかどうか
        if (!MyMaidData.isMainDBActive()) return;

        Location to = event.getTo();
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        World world = Bukkit.getServer().getWorld("Jao_Afa");
        if (world == null) return;
        Location prison = MyMaidData.paradiseLocation;
        if (!player.getLocation().getWorld().getUID().equals(world.getUID())) {
            player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたは南の楽園から出られません！");
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
        if (distance >= 40D) {
            player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたは南の楽園から出られません！");
            if (distance >= 50D) {
                if (!player.teleport(prison, TeleportCause.PLUGIN)) {
                    // 失敗時
                    Location oldBed = player.getBedSpawnLocation();
                    player.setBedSpawnLocation(prison, true);
                    player.setHealth(0);
                    player.setBedSpawnLocation(oldBed, true);
                }
            } else event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setRespawnLocation(MyMaidData.paradiseLocation);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.getLocation().getWorld().getName().equalsIgnoreCase("Jao_Afa")) return;
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたはブロックを置けません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたはブロックを置けません。");
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたはブロックを壊せません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたはブロックを壊せません。");
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたはブロックを着火できません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたはブロックを着火できません。");
    }

    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたは水や溶岩を撒けません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたは水や溶岩を撒けません。");
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたは水や溶岩を掬うことはできません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたは水や溶岩を掬うことはできません。");
    }

    @EventHandler
    public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
        player.sendMessage("[EBan] " + ChatColor.GREEN + "あなたはコマンドを実行できません。");
        Bukkit.getLogger().info("[EBan] " + player.getName() + "==>あなたはコマンドを実行できません。");
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        EBan eban = EBan.getInstance(player);
        // EBanされてる
        if (!eban.isStatus()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        EBan eban = EBan.getInstance(player);
        if (!eban.isStatus()) return; // EBanされてる
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EBan eban = EBan.getInstance(player);
        if (!eban.isStatus()) { // EBanされてる
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
            @Override
            public void run() {
                EBan.getInstance(player, true);
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}

