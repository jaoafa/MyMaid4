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
import com.jaoafa.mymaid4.lib.MCBans;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
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

        String reputation;
        MCBans mcbans = null;
        try {
            mcbans = new MCBans(player);
            if (mcbans.isFound()) {
                reputation = String.valueOf(mcbans.getReputation());
            } else {
                reputation = "null";
            }
        } catch (IOException e) {
            reputation = "null";
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
            builder.addField("評価値", reputation + " / 10", false);
            builder.addField("プレイヤー数", Bukkit.getOnlinePlayers().size() + "人", false);
            builder.addField("プレイヤー", "`" + String.join(", ", players) + "`", false);
            builder.setTimestamp(Instant.now());
            builder.setThumbnail(
                "https://crafatar.com/renders/body/" + player.getUniqueId() + ".png?overlay=true&scale=10");
            builder.setAuthor(Main.getMyMaidConfig().getJDA().getSelfUser().getName(), null,
                Main.getMyMaidConfig().getJDA().getSelfUser().getAvatarUrl());
            jaotan.sendMessageEmbeds(builder.build()).queue();

            if (mcbans != null && (mcbans.getGlobalCount() > 0 || mcbans.getLocalCount() > 0)) {
                int[] global_ids = mcbans.getGlobalBanIds();
                int[] local_ids = mcbans.getLocalBanIds();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("MCBans DATA : " + player.getName(),
                    "https://www.mcbans.com/player/" + player.getUniqueId().toString().replace("-", "") + "/");
                embed.setColor(Color.RED);
                embed.setDescription("Global: " + mcbans.getGlobalCount() + " / Local: " + mcbans.getLocalCount());
                embed.setTimestamp(Instant.now());

                for (int id : global_ids) {
                    try {
                        MCBans.Ban ban = new MCBans.Ban(id);
                        String reason = ban.getReason();
                        String date = ban.getDate();
                        String banned_by = ban.getBannedBy();
                        String server = ban.getServer();

                        embed.addField("[Global] `" + server + "`",
                            "Reason: " + reason + "\nBanned_by: `" + banned_by + "`\n" + date, false);
                    } catch (IOException e) {
                        embed.addField("BanID: " + id, "Failed to get the data", false);
                    }
                }
                for (int id : local_ids) {
                    try {
                        MCBans.Ban ban = new MCBans.Ban(id);
                        String reason = ban.getReason();
                        String date = ban.getDate();
                        String banned_by = ban.getBannedBy();
                        String server = ban.getServer();

                        embed.addField("[Local] `" + server + "`",
                            "Reason: " + reason + "\nBanned_by: `" + banned_by + "`\n" + date, false);
                    } catch (IOException e) {
                        embed.addField("BanID: " + id, "Failed to get the data", false);
                    }
                }

                jaotan.sendMessageEmbeds(embed.build()).queue();
            }
        }
    }
}