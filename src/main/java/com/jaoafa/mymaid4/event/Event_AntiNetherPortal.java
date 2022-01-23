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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

import java.text.MessageFormat;

public class Event_AntiNetherPortal extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "ネザーポータルの作成を制限します。";
    }

    @EventHandler
    public void onEntityCreatePortalEvent(PortalCreateEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }
        if (isAM(player)) {
            player.sendMessage(Component.text().append(
                Component.text("["),
                Component.text("AntiNetherPortal", NamedTextColor.RED),
                Component.text("]"),
                Component.space(),
                Component.text("ネザーポータルの作成・生成は運営以外制限されています。必要がない場合は破壊して下さい。", NamedTextColor.GREEN)
            ));
            return;
        }
        event.setCancelled(true);
        player.sendMessage(Component.text().append(
            Component.text("["),
            Component.text("AntiNetherPortal", NamedTextColor.RED),
            Component.text("]"),
            Component.space(),
            Component.text("負荷対策と生成による地形破壊の観点から、ネザーポータルの作成・生成を禁止しています。ご協力をお願いします。", NamedTextColor.GREEN)
        ));
        Location loc = event.getBlocks().isEmpty() ? null : event.getBlocks().get(0).getLocation();
        sendAM(Component.text().append(
            Component.text("["),
            Component.text("AntiNetherPortal", NamedTextColor.RED),
            Component.text("]"),
            Component.space(),
            Component.text(MessageFormat.format("{0}の近く({1})でネザーポータルが生成されましたが、規制されました。", player.getName(), loc != null ? formatLocation(loc) : "NULL"), NamedTextColor.GREEN)
        ).build());
    }
}
