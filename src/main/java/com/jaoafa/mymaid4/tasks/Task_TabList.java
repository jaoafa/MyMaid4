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

package com.jaoafa.mymaid4.tasks;

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMCJP;
import com.jaoafa.mymaid4.lib.PlayerVoteDataMono;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class Task_TabList extends BukkitRunnable {
    @Override
    public void run() {
        TextComponent header = Component.text().append(
            Component.text("jao", TextColor.color(241, 196, 15)),
            Component.space(),
            Component.text("Minecraft", TextColor.color(230, 126, 34)),
            Component.space(),
            Component.text("Server", TextColor.color(231, 76, 60)),
            Component.newline(),
            Component.text(MyMaidLibrary.sdfFormat(new Date()), TextColor.color(46, 204, 113)),
            Component.newline(),
            Component.text("Online: " + Bukkit.getServer().getOnlinePlayers().size() + " / " + Bukkit.getServer().getMaxPlayers(), TextColor.color(241, 196, 15)),
            Component.newline(),
            Component.text("Vote: %%%VOTE%%%", TextColor.color(230, 126, 34)),
            Component.newline(),
            Component.text("OnlineTime: %%%ONLINETIME%%%", TextColor.color(231, 76, 60))
        ).build();

        double[] tps = Bukkit.getServer().getTPS();
        TextComponent footer = Component.text().append(
            Component.text("TPS", TextColor.color(155, 89, 182), TextDecoration.BOLD),
            Component.newline(),
            Component.text("1m: ", TextColor.color(241, 196, 15)),
            Component.text(formatTPS(tps[0]), getTPSColor(tps[0])),
            Component.newline(),
            Component.text("5m: ", TextColor.color(231, 76, 60)),
            Component.text(formatTPS(tps[1]), getTPSColor(tps[1])),
            Component.newline(),
            Component.text("15m: ", TextColor.color(46, 204, 113)),
            Component.text(formatTPS(tps[2]), getTPSColor(tps[2]))
        ).build();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerVoteDataMCJP voteDataMCJP = new PlayerVoteDataMCJP(player);
            PlayerVoteDataMono voteDataMono = new PlayerVoteDataMono(player);
            String voteText = voteDataMCJP.getVoteCount() + " | " + voteDataMono.getVoteCount();
            String onlineTime = formatDateTime(player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20);

            Component userHeader = header
                .replaceText(TextReplacementConfig.builder()
                    .match("%%%VOTE%%%")
                    .replacement(voteText).build())
                .replaceText(TextReplacementConfig.builder()
                    .match("%%%ONLINETIME%%%")
                    .replacement(onlineTime).build());
            player.sendPlayerListHeaderAndFooter(userHeader, footer);
        }
    }

    String formatTPS(double tps) {
        return String.format("%.2f%s", tps, tps > 20.0 ? "*" : "");
    }

    TextColor getTPSColor(double tps) {
        if (tps > 18.0) {
            return NamedTextColor.GREEN;
        }
        if (tps > 16.0) {
            return NamedTextColor.YELLOW;
        }
        return NamedTextColor.RED;
    }

    // https://www.geeksforgeeks.org/converting-seconds-into-days-hours-minutes-and-seconds/
    String formatDateTime(int sec) {
        int days = sec / (24 * 3600);

        sec = sec % (24 * 3600);
        int hours = sec / 3600;

        sec %= 3600;
        int minutes = sec / 60;

        sec %= 60;
        int seconds = sec;

        return String.format("%d日%d時間%d分%d秒", days, hours, minutes, seconds);
    }
}
