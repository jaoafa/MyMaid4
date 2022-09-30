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
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Event_FirstLogin extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "初めてログインしたプレイヤーを通知します。";
    }

    @EventHandler
    public void OnEvent_FirstLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            return; // 初めてではない
        }

        if (Main.getMyMaidConfig().isDevelopmentServer()) {
            Main.getMyMaidLogger().warning("開発サーバのため、新規ログイン通知機能は動作しません。");
            return;
        }

        MCBansCache mcbans = null;
        try {
            mcbans = MCBansCache.get(player);
        } catch (IOException ignored) {
        }

        List<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p.getName());
        }

        if (Main.getMyMaidConfig().getJDA() != null) {
            TextChannel jaotan = MyMaidData.getJaotanChannel();
            if (jaotan == null) {
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("NEW PLAYER JOIN", "https://users.jaoafa.com/" + player.getUniqueId());
            builder.appendDescription("新規プレイヤー(`" + player.getName() + "`)がサーバにログインしました！");
            builder.setColor(Color.GREEN);
            builder.addField("プレイヤーID", "`" + player.getName() + "`", false);
            builder.addField("評価値", (mcbans != null ? mcbans.getReputation() : "NULL") + " / 10", false);
            builder.addField("プレイヤー数", Bukkit.getOnlinePlayers().size() + "人", false);
            builder.addField("プレイヤー", "`" + String.join(", ", players) + "`", false);
            builder.setTimestamp(Instant.now());
            builder.setThumbnail(
                "https://crafatar.com/renders/body/" + player.getUniqueId() + ".png?overlay=true&scale=10");
            builder.setAuthor(Main.getMyMaidConfig().getJDA().getSelfUser().getName(), null,
                Main.getMyMaidConfig().getJDA().getSelfUser().getAvatarUrl());
            jaotan.sendMessageEmbeds(builder.build()).queue();

            if (mcbans != null && (mcbans.getGlobalCount() > 0 || mcbans.getLocalCount() > 0)) {
                List<MCBansCache.Ban> globalBans = mcbans.getGlobalBans();
                List<MCBansCache.Ban> localBans = mcbans.getLocalBans();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("MCBans DATA : " + player.getName(),
                    "https://www.mcbans.com/player/" + player.getUniqueId().toString().replace("-", "") + "/");
                embed.setColor(Color.RED);
                embed.setDescription("Global: " + mcbans.getGlobalCount() + " / Local: " + mcbans.getLocalCount());
                embed.setTimestamp(Instant.now());

                for (MCBansCache.Ban ban : globalBans) {
                    try {
                        String reason = ban.reason();
                        String bannedAt = MyMaidLibrary.sdfFormat(ban.bannedAt());
                        MCBansCache.BanDetails details = ban.retrieveDetails();
                        if (details == null) {
                            embed.addField("BanID: " + ban.banId(), "Failed to get the data", false);
                            return;
                        }
                        String serverAddress = details.serverAddress();
                        String bannedByName = details.bannedByName();

                        embed.addField("[Global] `" + serverAddress + "`",
                            "Reason: " + reason + "\nBanned_by: `" + bannedByName + "`\n" + bannedAt, false);
                    } catch (IOException e) {
                        embed.addField("BanID: " + ban.banId(), "Failed to get the data", false);
                    }
                }
                for (MCBansCache.Ban ban : localBans) {
                    try {
                        String reason = ban.reason();
                        String bannedAt = MyMaidLibrary.sdfFormat(ban.bannedAt());
                        MCBansCache.BanDetails details = ban.retrieveDetails();
                        if (details == null) {
                            embed.addField("BanID: " + ban.banId(), "Failed to get the data", false);
                            return;
                        }
                        String serverAddress = details.serverAddress();
                        String bannedByName = details.bannedByName();

                        embed.addField("[Local] `" + serverAddress + "`",
                            "Reason: " + reason + "\nBanned_by: `" + bannedByName + "`\n" + bannedAt, false);
                    } catch (IOException e) {
                        embed.addField("BanID: " + ban.banId(), "Failed to get the data", false);
                    }
                }

                jaotan.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}