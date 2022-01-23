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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class Event_NotifyKick extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "キックされた際にAdminとModeratorに理由を通知します。";
    }

    @EventHandler(priority = EventPriority.MONITOR,
                  ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        String reason = PlainTextComponentSerializer.plainText().serialize(event.reason());
        sendAM(Component.text().append(
            Component.text("[Kick]"),
            Component.space(),
            Component.text("プレイヤー「", NamedTextColor.GREEN),
            Component.text(event.getPlayer().getName(), NamedTextColor.GREEN),
            Component.text("」は「", NamedTextColor.GREEN),
            event.reason(),
            Component.text(" (", NamedTextColor.GREEN),
            Component.text(reason)
                .hoverEvent(HoverEvent.showText(Component.text("PlayerKickEvent.Cause." + event.getCause().name())))
                .clickEvent(ClickEvent.copyToClipboard(reason)),
            Component.text(")", NamedTextColor.GREEN),
            Component.text("」という理由でキックされました。", NamedTextColor.GREEN)
        ).build());
    }
}