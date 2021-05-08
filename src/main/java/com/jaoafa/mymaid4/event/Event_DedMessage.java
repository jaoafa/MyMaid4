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

import com.jaoafa.mymaid4.lib.DedMessage;
import com.jaoafa.mymaid4.lib.EventPremise;
import com.jaoafa.mymaid4.lib.MyMaidLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Event_DedMessage extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "dedmessageコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        Player killer = player.getKiller();
        EntityType entityType = player.getLastDamageCause() != null ? player.getLastDamageCause().getEntityType() : null;
        Entity entity = player.getLastDamageCause() != null ? player.getLastDamageCause().getEntity() : null;

        DedMessage.Details details = DedMessage.match(loc.toVector());
        if (details == null) {
            return;
        }
        String deathMessage = details.getMessage();
        if (deathMessage == null) {
            event.setCancelled(true);
            return;
        }

        deathMessage = deathMessage.replaceAll("%player%", player.getName());
        if (entityType == EntityType.PLAYER && killer != null) {
            deathMessage = deathMessage.replaceAll("%killer%", killer.getName());
            event.deathMessage(Component.text(deathMessage));
            return;
        }
        if (entityType != null && entity != null) {
            deathMessage = deathMessage.replaceAll("%killer%", entity.getName());
            event.deathMessage(Component.text(deathMessage));
            return;
        }

        deathMessage = deathMessage.replaceAll("%killer%", "？");
        event.deathMessage(Component.text(deathMessage));
    }
}