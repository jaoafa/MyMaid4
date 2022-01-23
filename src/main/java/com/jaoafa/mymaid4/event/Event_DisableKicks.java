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

import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class Event_DisableKicks extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "不要なキックを無効化します。";
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        String reason = PlainTextComponentSerializer.plainText().serialize(event.reason());
        if (reason.equals("disconnect.spam") || event.getCause() == PlayerKickEvent.Cause.SPAM) {
            event.setCancelled(true);
        }
        if (reason.startsWith("You dropped your items too quickly")) {
            event.setCancelled(true);
        }
    }
}