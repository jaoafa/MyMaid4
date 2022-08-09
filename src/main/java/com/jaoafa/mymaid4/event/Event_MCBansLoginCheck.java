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
import com.jaoafa.mymaid4.lib.MCBansCache;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.UUID;

public class Event_MCBansLoginCheck extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ログイン時、MCBansのキャッシュデータベースを確認しGBanされていたりReputationが足りなくないかを確認します。";
    }

    @EventHandler
    public void OnLoginCheck(AsyncPlayerPreLoginEvent event) {
        // MCBansが落ちている場合を考慮してjaoafaデータベースからチェック

        // Reputationチェック
        String name = event.getName();
        UUID uuid = event.getUniqueId();
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        MCBansCache mcbans;
        try {
            mcbans = MCBansCache.get(player);
        } catch (IOException e) {
            Main.getMyMaidLogger().warning("MCBansキャッシュサーバへの接続に失敗しました: %s".formatted(e.getMessage()));
            return;
        }
        if (mcbans == null) {
            return;
        }

        if (mcbans.getReputation() < 3) {
            // 3未満は規制
            Component component = Component.text().append(
                Component.text("----- MCBans Checker -----", NamedTextColor.RED),
                Component.newline(),
                Component.text("Access denied.", NamedTextColor.WHITE),
                Component.newline(),
                Component.text("Your reputation is below this server's threshold.", NamedTextColor.WHITE)
            ).build();
            event.disallow(Result.KICK_BANNED, component);
            return;
        }
        if (mcbans.getReputation() != 10) {
            sendAMR(Component.text().append(
                Component.text("[MCBansChecker]", NamedTextColor.RED),
                Component.space(),
                Component.text(name, NamedTextColor.GREEN),
                Component.space(),
                Component.text("reputation: ", NamedTextColor.GREEN),
                Component.space(),
                Component.text(mcbans.getReputation(), NamedTextColor.GREEN)
            ).build());
        }
    }

    @EventHandler
    public void OnLoginAfterCheck(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                MCBansCache mcbans;
                try {
                    mcbans = MCBansCache.get(player);
                } catch (IOException e) {
                    Main.getMyMaidLogger().warning("MCBansキャッシュサーバへの接続に失敗しました: %s".formatted(e.getMessage()));
                    return;
                }
                if (mcbans == null) {
                    return;
                }

                sendAMR(Component.text().append(
                    Component.text("[MCBans]"),
                    Component.space(),
                    Component.text(String.format("%s -> %s (G:%d | L:%d)", player.getName(), mcbans.getReputation(), mcbans.getGlobalCount(), mcbans.getLocalCount()), NamedTextColor.GRAY)
                ).build());
                if (mcbans.getReputation() < 3) {
                    // 3未満はキック
                    Component component = Component.text().append(
                        Component.text("----- MCBans Checker -----", NamedTextColor.RED),
                        Component.newline(),
                        Component.text("Access denied.", NamedTextColor.WHITE),
                        Component.newline(),
                        Component.text("Your reputation is below this server's threshold.", NamedTextColor.WHITE)
                    ).build();
                    new BukkitRunnable() {
                        public void run() {
                            player.kick(component);
                        }
                    }.runTask(Main.getJavaPlugin());
                }
            }
        }.runTaskAsynchronously(Main.getJavaPlugin());
    }
}
