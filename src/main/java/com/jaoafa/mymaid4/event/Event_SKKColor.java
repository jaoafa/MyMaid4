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
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Event_SKKColor extends MyMaidLibrary implements Listener {
    /*public void onEvent_ChatSKK(AsyncChatEvent event) {
        Player player = event.getPlayer();
        ChatComposer composer = (_player, displayName, message) -> Component.translatable("chat.type.text", Component.text().append(
            Component.text("■", SKKColorManager.getPlayerColor(player)),
            displayName
        ), message);
        event.composer(composer);
    }*/

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_ChatSKK(AsyncPlayerChatEvent event) {
        event.setFormat(
            event.getFormat().replaceFirst("%1\\$s", String.format("%s■%s%s", SKKColorManager.getPlayerChatColor(event.getPlayer()), ChatColor.WHITE, event.getPlayer().getName()))
        );
    }
}
