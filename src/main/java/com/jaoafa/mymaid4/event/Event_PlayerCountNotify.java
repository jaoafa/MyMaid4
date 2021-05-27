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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Event_PlayerCountNotify extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "入退出時、プレイヤー数を表示します。";
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        int count = Bukkit.getOnlinePlayers().size();
        int countExcludeHide = Math.toIntExact(Bukkit.getOnlinePlayers().stream().filter(p -> !MyMaidData.isHid(p.getUniqueId())).count());
        if (count == countExcludeHide) {
            chatFake(NamedTextColor.GOLD, "jaotan", "現在『" + count + "人』がログインしています", false);
            return;
        }
        sendAMR(getChatFake(NamedTextColor.GOLD, "jaotan", "現在『" + count + "人』がログインしています"));
        sendVD(getChatFake(NamedTextColor.GOLD, "jaotan", "現在『" + countExcludeHide + "人』がログインしています"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        int count = Bukkit.getOnlinePlayers().size() - 1;
        int countExcludeHide = Math.toIntExact(Bukkit.getOnlinePlayers().stream().filter(p -> !MyMaidData.isHid(p.getUniqueId())).count()) - 1;
        if (count == countExcludeHide) {
            chatFake(NamedTextColor.GOLD, "jaotan", "現在『" + count + "人』がログインしています", false);
            return;
        }
        sendAMR(getChatFake(NamedTextColor.GOLD, "jaotan", "現在『" + count + "人』がログインしています"));
        sendVD(getChatFake(NamedTextColor.GOLD, "jaotan", "現在『" + countExcludeHide + "人』がログインしています"));
    }
}
