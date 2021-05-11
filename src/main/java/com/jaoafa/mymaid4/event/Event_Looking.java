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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.stream.Collectors;

public class Event_Looking extends MyMaidLibrary implements Listener, EventPremise {
    @Override
    public String description() {
        return "lookingコマンドに関する処理を行います。";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().toBlockLocation() == event.getTo().toBlockLocation()) {
            return;
        }
        Player player = event.getPlayer();
        // 誰かが見ている
        Set<Player> looking = MyMaidData.getLookingMe(player.getUniqueId()).stream()
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());
        for (Player p : looking) {
            if (player.getWorld() != p.getWorld()) {
                return;
            }
            if (player.getLocation().toBlockLocation().equals(p.getLocation().toBlockLocation())) {
                return;
            }
            Vector vector = p.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
            vector.setX(-vector.getX());
            vector.setY(-vector.getY());
            vector.setZ(-vector.getZ());
            Location teleportTo = p.getLocation().setDirection(vector);
            if (Float.isInfinite(teleportTo.getPitch()) || Float.isInfinite(teleportTo.getYaw())) {
                return;
            }
            p.teleport(teleportTo);
        }
    }
}
