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
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import com.jaoafa.mymaid4.lib.SKKColorManager;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Date;

public class Event_SKKColor extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "チャット欄に表示される四角色に関する処理を行います。";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_ChatSKK(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // これでいいのだろうか…
        ChatRenderer renderer = (_player, displayName, message, viewer) ->
            Component.text().append(
                Component.text("[", NamedTextColor.GRAY),
                Component.text(sdfTimeFormat(new Date()), NamedTextColor.GRAY),
                Component.text("]", NamedTextColor.GRAY),
                Component.text("■", SKKColorManager.getPlayerColor(player)),
                displayName,
                Component.text(": "),
                MyMaidLibrary.replaceComponentURL(message)).build();
        event.renderer(renderer);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent_JoinTabReload(PlayerJoinEvent event) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) SKKColorManager.setPlayerSKKTabList(p);
    }
}
