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

import com.jaoafa.mymaid4.customEvents.TeleportCommandEvent;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidData;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.MessageFormat;

public class Event_NotifyTPDiscord extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "tpコマンドによるテレポート時にDiscord#server-chatでもそれを表示します。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void onTeleportCommand(TeleportCommandEvent event) {
        Player toPlayer = event.getToPlayer();
        if (toPlayer == null) {
            return;
        }
        if (MyMaidData.getServerChatChannel() == null) {
            return;
        }
        MyMaidData.getServerChatChannel().sendMessage(MessageFormat.format("*[{0}: {1} teleported to {2}]*", DiscordEscape(event.getSender().getName()), DiscordEscape(event.getFromPlayer().getName()), DiscordEscape(event.getToPlayer().getName()))).queue();
    }
}