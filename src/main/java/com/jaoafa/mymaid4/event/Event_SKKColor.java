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

import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SKKColorManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Event_SKKColor extends MyMaidLibrary implements Listener {
    // TODO 非推奨イベントのため変更する必要がありますが、使い方が分からないので困り果てています
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_ChatSKK(AsyncPlayerChatEvent event) {
        event.setFormat(
            event.getFormat().replaceFirst("%1\\$s",String.format("%s■%s%s",SKKColorManager.getPlayerChatColor(event.getPlayer()), ChatColor.WHITE,event.getPlayer().getName()))
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_JoinChangeMessage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Component joinMessage = SKKColorManager.getPlayerSKKJoinMessage(player);
        if (joinMessage != null) {
            event.joinMessage(joinMessage);
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            SKKColorManager.setPlayerSKKTabList(p);
        }
    }
}
