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
import com.jaoafa.mymaid4.lib.ChatBan;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Event_ChatBan implements Listener, EventPremise {
    private final List<String> tell1v1 = List.of(
        "tell",
        "msg",
        "message",
        "m",
        "t",
        "r",
        "w"
    );

    @Override
    public String description() {
        return "ChatBanに関する各種処理を行います。";
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Component component = event.message();
        String message = PlainTextComponentSerializer.plainText().serialize(component);
        ChatBan chatBan = ChatBan.getInstance(player);

        if (!chatBan.isStatus()) return;
        String reason = chatBan.getReason();

        player.sendMessage("[ChatJail] " + ChatColor.RED + "あなたは、「" + reason + "」という理由でチャット規制をされています。");
        player.sendMessage("[ChatJail] " + ChatColor.RED + "解除申請の方法や、Banの方針などは以下ページをご覧ください。");
        player.sendMessage("[ChatJail] " + ChatColor.RED + "https://jaoafa.com/rule/management/ban");
        chatBan.addMessageDB(message);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void OnEvent_LoginChatBanCheck(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!MyMaidData.isMainDBActive()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                ChatBan chatBan = ChatBan.getInstance(player);
                if (!chatBan.isStatus()) return;
                String reason = chatBan.getReason();
                if (reason == null) return;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!MyMaidLibrary.isAMR(p)) continue;
                    p.sendMessage(
                        String.format("[ChatBan] %sプレイヤー「%s」は、「%s」という理由でChatBanされています。", ChatColor.GREEN, player.getName(), reason));
                    p.sendMessage(
                        String.format("[ChatBan] %s詳しい情報は /chatban status %s でご確認ください。", ChatColor.GREEN, player.getName()));
                }
                player.sendMessage(String.format("[ChatBan] %sあなたは、「%s」という理由でChatBan(チャット規制)されています。", ChatColor.GREEN, reason));
                player.sendMessage(String.format("[ChatBan] %s解除申請の方法や、Banの方針などは以下ページをご覧ください。", ChatColor.GREEN));
                player.sendMessage(String.format("[ChatBan] %shttps://jaoafa.com/rule/management/punishment", ChatColor.GREEN));
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        ChatBan chatBan = ChatBan.getInstance(player);
        // ChatBanされてる
        if (!chatBan.isStatus()) return;
        String command = event.getMessage();
        if (!command.toLowerCase().startsWith("/chatban") && tell1v1.stream().map(c -> "/" + c).noneMatch(command::startsWith))
            return;
        event.setCancelled(true);
        player.sendMessage("[ChatBan] " + ChatColor.GREEN + "あなたはコマンドを実行できません。");
        Bukkit.getLogger().info("[ChatBan] " + player.getName() + "==>あなたはコマンドを実行できません。");
    }

    @EventHandler
    public void onJoinClearCache(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatBan chatBan = ChatBan.getInstance(player);
                chatBan.fetchData(false);
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }

    @EventHandler
    public void onQuitClearCache(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatBan.getInstance(player, true);
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}

